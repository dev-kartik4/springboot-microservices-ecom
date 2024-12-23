package com.shoppix.merchant_service_reactive.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppix.merchant_service_reactive.events.MerchantEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
public class MerchantKafkaConsumerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MerchantKafkaConsumerService.class);

    private final ObjectMapper objectMapper;
    private final MerchantService merchantService;
    private final MerchantKafkaProducerService merchantKafkaProducerService;

    @Autowired
    public MerchantKafkaProducerService(ObjectMapper objectMapper, MerchantService productService, MerchantKafkaProducerService merchantKafkaProducerService) {
        this.objectMapper = objectMapper;
        this.merchantService = merchantService;
        this.merchantKafkaProducerService = merchantKafkaProducerService;
    }

    @Retryable(
            value = { Exception.class },
            maxAttempts = 5,
            backoff = @Backoff(delay = 1000, maxDelay = 3000)
    )
    @KafkaListener(topics = "${spring.kafka.topic.product-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeMessageFromInventory(String topic, String message) throws JsonProcessingException {
        LOGGER.info("Received Message: {}", message);
        MerchantEvent inventoryEvent = objectMapper.readValue(message, MerchantEvent.class);
        try{
            LOGGER.info("Received from Inventory Topic {}, Message {}", topic,message);
            if(inventoryEvent.getInventoryMessageType().equals(InventoryEnum.INVENTORY_UPDATED.name())){
                productService.updateProductWhenInventoryUpdated(inventoryEvent.getInventory());
            }
        } catch(Exception e){

        }
    }

    @DltHandler
    @KafkaListener(topics = "${spring.kafka.dead-letter.inventory-dlt}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenDLTForInventory(InventoryEvent inventoryEvent, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic, @Header(KafkaHeaders.OFFSET) long offset) {
        LOGGER.info("Event on dlt topic={}, message={}", topic, inventoryEvent);
    }

}
