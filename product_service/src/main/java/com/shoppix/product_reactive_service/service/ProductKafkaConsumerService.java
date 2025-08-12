package com.shoppix.product_reactive_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppix.product_reactive_service.enums.InventoryEnum;
import com.shoppix.product_reactive_service.enums.MerchantEnum;
import com.shoppix.product_reactive_service.events.InventoryEvent;
import com.shoppix.product_reactive_service.enums.MerchantProductEnum;
import com.shoppix.product_reactive_service.events.MerchantProductEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
public class ProductKafkaConsumerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductKafkaConsumerService.class);

    private final ObjectMapper objectMapper;
    private final ProductService productService;
    private final ProductKafkaProducerService productKafkaProducerService;

    @Autowired
    public ProductKafkaConsumerService(ObjectMapper objectMapper, ProductService productService, ProductKafkaProducerService productKafkaProducerService) {
        this.objectMapper = objectMapper;
        this.productService = productService;
        this.productKafkaProducerService = productKafkaProducerService;
    }

    @Retryable(
            value = { Exception.class },
            maxAttempts = 5,
            backoff = @Backoff(delay = 1000, maxDelay = 3000)
    )
    @KafkaListener(topics = "${spring.kafka.topic.inventory-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeMessageFromInventory(String topic, String message) throws JsonProcessingException {
        LOGGER.info("Received Message: {}", message);
        InventoryEvent inventoryEvent = objectMapper.readValue(message, InventoryEvent.class);
        try{
            LOGGER.info("Received from Inventory Topic {}, Message {}", topic,message);
            if(inventoryEvent.getInventoryMessageType().equals(InventoryEnum.PRODUCT_INVENTORY_UPDATED.name())){
                productService.updateProductWhenInventoryUpdated(inventoryEvent.getInventory());
            }
        } catch(Exception e){
            LOGGER.error("ERROR DURING CONSUMING MESSAGES FROM INVENTORY MICROSERVICE");
        }
    }

    @Retryable(
            value = { Exception.class },
            maxAttempts = 5,
            backoff = @Backoff(delay = 1000, maxDelay = 3000)
    )
    @KafkaListener(topics = "${spring.kafka.topic.merchant-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeMessageFromMerchant(String topic, String message) throws JsonProcessingException {
        LOGGER.info("Received Message: {}", message);
        MerchantProductEvent merchantProductEvent = objectMapper.readValue(message, MerchantProductEvent.class);
        try{
            LOGGER.info("Received from Merchant Topic to Product {}, Message {}", topic,message);
            if(merchantProductEvent.getMerchantMessageType().equals(MerchantEnum.MERCHANT_DELETED.name())){
                productService.deleteProductsConnectedWithMerchantId(merchantProductEvent.getMerchantId());
            }
        } catch(Exception e){
            LOGGER.error("PRODUCT: ERROR DURING CONSUMING MESSAGES FROM MERCHANT MICROSERVICE");
        }
    }

//    @DltHandler
//    @KafkaListener(topics = "${spring.kafka.dead-letter.product-topic-dlt}", groupId = "${spring.kafka.consumer.group-id}")
//    public void listenDLTForProduct(ProductEvent productEvent, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic, @Header(KafkaHeaders.OFFSET) long offset) {
//        LOGGER.info("Event on dlt topic={}, message={}", topic, productEvent);
//    }
//
//    @DltHandler
//    //@KafkaListener(topics = "${spring.kafka.dead-letter.inventory-dlt}", groupId = "${spring.kafka.consumer.group-id}")
//    public void listenDLTForInventory(InventoryEvent inventoryEvent, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic, @Header(KafkaHeaders.OFFSET) long offset) {
//        LOGGER.info("Event on dlt topic={}, message={}", topic, inventoryEvent);
//    }


}


