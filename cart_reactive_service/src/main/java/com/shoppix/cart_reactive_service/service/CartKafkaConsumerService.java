package com.shoppix.cart_reactive_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppix.cart_reactive_service.enums.CartEnum;
import com.shoppix.cart_reactive_service.enums.CartProductEnum;
import com.shoppix.cart_reactive_service.events.CartEvent;
import com.shoppix.cart_reactive_service.enums.CustomerEnum;
import com.shoppix.cart_reactive_service.events.CartProductEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.scheduler.Schedulers;

@Service
@Component
public class CartKafkaConsumerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CartKafkaConsumerService.class);

    private final ObjectMapper objectMapper;
    @Autowired
    private final CartService cartService;
    @Autowired
    private final CartKafkaProducerService cartKafkaProducerService;

    @Autowired
    public CartKafkaConsumerService(ObjectMapper objectMapper, CartService cartService, CartKafkaProducerService cartKafkaProducerService) {
        this.objectMapper = objectMapper;
        this.cartService = cartService;
        this.cartKafkaProducerService = cartKafkaProducerService;
    }

    @Retryable(
            value = { Exception.class },
            maxAttempts = 5,
            backoff = @Backoff(delay = 1000, maxDelay = 3000)
    )
    @KafkaListener(topics = "${spring.kafka.topic.cart-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeMessage(String message) throws JsonProcessingException {
        CartEvent cartEvent = objectMapper.readValue(message, CartEvent.class);
        CartProductEvent cartProductEvent = objectMapper.readValue(message, CartProductEvent.class);

        LOGGER.info("Received Message: {}", message);
        try {

            if (cartEvent.getCartMessageType().equals(CustomerEnum.CUSTOMER_REGISTERED.name())) {
                // Call createOrUpdateCart in a non-blocking way, using subscribe to trigger the database update
                cartService.createOrUpdateCart(cartEvent.getCartMessage())
                        .doOnSuccess(cart -> LOGGER.info("CART CREATED SUCCESSFULLY FOR CUSTOMER ID [{}]", cartEvent.getCustomerIdForCart()))
                        .doOnError(error -> LOGGER.error("FAILED TO CREATE CART FOR CUSTOMER ID[{}]", cartEvent.getCartMessage().getCustomerIdForCart(), error))
                        .subscribe();  // Trigger subscription to make the update happen
            }
            if (cartEvent.getCartMessageType().equals(CustomerEnum.CUSTOMER_DELETED.name())) {
                cartService.deleteCartWhenCustomerIsDeleted(cartEvent.getCustomerIdForCart())
                        .publishOn(Schedulers.parallel()) // Parallel execution (if needed)
                        .doOnTerminate(() -> LOGGER.info("Cart deletion process completed for customer ID [{}]", cartEvent.getCustomerIdForCart()))
                        .doOnError(error -> LOGGER.error("Error during cart deletion for customer ID [{}]", cartEvent.getCustomerIdForCart(), error))
                        .subscribe(); // Trigger the operation by subscribing
            }
            if(cartEvent.getCartMessageType().equals(CartProductEnum.ADD_CART_PRODUCT.name())){
                cartService.addProductToCart(cartEvent.getCartProductEvent()).subscribe();
            }
            if(cartEvent.getCartMessageType().equals(CartProductEnum.REMOVE_CART_PRODUCT.name()) || cartEvent.getCartMessageType().equals(CartProductEnum.REMOVE_ALL_CART_PRODUCTS.name())){
                cartService.removeProductsFromCart(cartEvent.getCartProductEvent()).subscribe();
            }


        } catch (Exception e) {
            LOGGER.error("Error processing cart event", e);
            // Handle failure, send the message to the dead-letter topic
            cartEvent.setCartMessageType(CartEnum.CART_FAILED.name());
            //String cartAsMessage = objectMapper.writeValueAsString(cartEvent);
            //cartKafkaProducerService.sendMessage("${spring.kafka.dead-letter.cart-dlt}", cartAsMessage);
        }
    }

    @DltHandler
    @KafkaListener(topics = "${spring.kafka.dead-letter.cart-dlt}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenDLT(String topic,String message) {
        LOGGER.info("Event on dlt topic={}, message={}", topic,message);
    }
}
