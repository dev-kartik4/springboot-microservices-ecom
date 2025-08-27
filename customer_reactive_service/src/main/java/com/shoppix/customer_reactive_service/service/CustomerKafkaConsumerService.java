package com.shoppix.customer_reactive_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppix.customer_reactive_service.enums.CartEnum;
import com.shoppix.customer_reactive_service.events.CartEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
public class CustomerKafkaConsumerService {

    @Value("${spring.kafka.topic.cart-topic}")
    private String cartTopic;

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerKafkaConsumerService.class);

    private final ObjectMapper objectMapper;
    private final CustomerKafkaProducerService customerKafkaProducerService;

    @Autowired
    public CustomerKafkaConsumerService(ObjectMapper objectMapper, CustomerService customerService, CustomerKafkaProducerService customerKafkaProducerService) {
        this.objectMapper = objectMapper;
        this.customerKafkaProducerService = customerKafkaProducerService;
    }

    @Retryable(
            value = { Exception.class },
            maxAttempts = 5,
            backoff = @Backoff(delay = 1000, maxDelay = 3000)
    )
    @KafkaListener(topics = "${spring.kafka.topic.cart-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeMessageFromCart(String topic, String message) throws JsonProcessingException {
        CartEvent cartEvent = objectMapper.readValue(message, CartEvent.class);

        try{
            LOGGER.info("Received from Cart Topic {}, Message {}", topic,message);

//            if(cartEvent.getCartMessageType().equals(CartEnum.CART_CREATED.name())){
//                customerKafkaProducerService.sendMessage("${spring.kafka.topic.notification-topic}");
//            }
        } catch(Exception e){
            cartEvent.setCartMessageType(CartEnum.CART_FAILED.name());
            String cartAsMessage = objectMapper.writeValueAsString(cartEvent);
            customerKafkaProducerService.sendMessage("${spring.kafka.dead-letter.cart-dlt}",cartAsMessage);
        }
    }

    @DltHandler
    @KafkaListener(topics = "${spring.kafka.dead-letter.cart-dlt}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenDLTForCart(CartEvent cartEvent, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic, @Header(KafkaHeaders.OFFSET) long offset) {
        LOGGER.info("Event on dlt topic={}, message={}", topic, cartEvent);
    }

}
