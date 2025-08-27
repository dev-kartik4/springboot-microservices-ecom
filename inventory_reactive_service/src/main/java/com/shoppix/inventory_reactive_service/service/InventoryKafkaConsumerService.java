package com.shoppix.inventory_reactive_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppix.inventory_reactive_service.enums.InventoryEnum;
import com.shoppix.inventory_reactive_service.enums.MerchantEnum;
import com.shoppix.inventory_reactive_service.enums.ProductEnum;
import com.shoppix.inventory_reactive_service.events.InventoryEvent;
import com.shoppix.inventory_reactive_service.events.MerchantProductEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Component
public class InventoryKafkaConsumerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryKafkaConsumerService.class);

    private final ObjectMapper objectMapper;
    @Autowired
    private final InventoryService inventoryService;

    @Autowired
    private final InventoryKafkaProducerService inventoryKafkaProducerService;

    @Autowired
    public InventoryKafkaConsumerService(InventoryService inventoryService, InventoryKafkaProducerService inventoryKafkaProducerService,ObjectMapper objectMapper) {
        this.inventoryService = inventoryService;
        this.inventoryKafkaProducerService = inventoryKafkaProducerService;
        this.objectMapper = objectMapper;
    }

    @Retryable(
            value = { Exception.class },
            maxAttempts = 5,
            backoff = @Backoff(delay = 1000, maxDelay = 3000)
    )
    @KafkaListener(topics = "${spring.kafka.topic.inventory-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeMessage(String message) throws JsonProcessingException {
        InventoryEvent inventoryEvent = objectMapper.readValue(message, InventoryEvent.class);
        LOGGER.info("Received Message: {}", message);
        try{
            if(inventoryEvent.getInventoryMessageType().equals(ProductEnum.PRODUCT_CREATE_SUCCESS.name())){
                LOGGER.info("RECEIVED INVENTORY OBJECT ["+inventoryEvent.getInventory()+"]");
                inventoryService.saveOrUpdateInventory(inventoryEvent.getInventory()).subscribe();
            }
        } catch (Exception e) {
            LOGGER.error("Error processing inventory event", e);
            // Handle failure, send the message to the dead-letter topic
            inventoryEvent.setInventoryMessageType(InventoryEnum.PRODUCT_INVENTORY_INIT_FAILED.name());
            //String cartAsMessage = objectMapper.writeValueAsString(cartEvent);
            //cartKafkaProducerService.sendMessage("${spring.kafka.dead-letter.cart-dlt}", cartAsMessage);
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
            LOGGER.info("Received from Merchant Topic To Inventory {}, Message {}", topic,message);
            if(merchantProductEvent.getMerchantMessageType().equals(MerchantEnum.MERCHANT_DELETED.name())){
                inventoryService.deleteInventoryConnectedToMerchant(merchantProductEvent.getMerchantId());
            }
        } catch(Exception e){
            LOGGER.error("INVENTORY: ERROR DURING CONSUMING MESSAGES FROM MERCHANT MICROSERVICE");
        }
    }

    @DltHandler
    @KafkaListener(topics = "${spring.kafka.dead-letter.inventory-dlt}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenDLT(String topic,String message) {
        LOGGER.info("Event on dlt topic={}, message={}", topic,message);
    }
}
