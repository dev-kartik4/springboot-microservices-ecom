package com.shoppix.merchant_service_reactive.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppix.merchant_service_reactive.entity.MerchantDetails;
import com.shoppix.merchant_service_reactive.enums.MerchantProductEnum;
import com.shoppix.merchant_service_reactive.events.MerchantProductEvent;
import com.shoppix.merchant_service_reactive.repo.MerchantRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Component
public class MerchantKafkaConsumerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MerchantKafkaConsumerService.class);

    @Autowired
    private MerchantService merchantService;
    private final ObjectMapper objectMapper;
    private final MerchantRepo merchantRepo;
    private final MerchantKafkaProducerService merchantKafkaProducerService;

    @Autowired
    public MerchantKafkaConsumerService(ObjectMapper objectMapper, MerchantService merchantService, MerchantRepo merchantRepo,MerchantKafkaProducerService merchantKafkaProducerService) {
        this.objectMapper = objectMapper;
        this.merchantService = merchantService;
        this.merchantRepo = merchantRepo;
        this.merchantKafkaProducerService = merchantKafkaProducerService;
    }

    @Retryable(
            value = { Exception.class },
            maxAttempts = 5,
            backoff = @Backoff(delay = 1000, maxDelay = 3000)
    )
    @KafkaListener(topics = "${spring.kafka.topic.merchant-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeMessage(String topic, String message) throws JsonProcessingException {
        LOGGER.info("Received Message: {}", message);
        MerchantProductEvent merchantProductEvent = objectMapper.readValue(message, MerchantProductEvent.class);
        try{
            LOGGER.info("Received from Merchant Topic {}, Message {}", topic,message);
            if(merchantProductEvent.getMerchantMessageType().equals(MerchantProductEnum.MERCHANT_PRODUCT_UPDATE.name())){
                Mono<MerchantDetails> merchantData = merchantService.getMerchantById(merchantProductEvent.getMerchantId());

                merchantData.subscribe(merchant -> {
                    merchant.getListOfProductsByMerchant().add(merchantProductEvent.getMerchantProduct());
                    merchantService.createOrUpdateMerchantDetails(merchant).subscribe();
                });
            }
        } catch(Exception e){
            LOGGER.error("ERROR DURING UPDATING MERCHANT SPECIFIC DATA", e);
        }
    }

//    @DltHandler
//    @KafkaListener(topics = "${spring.kafka.dead-letter.inventory-dlt}", groupId = "${spring.kafka.consumer.group-id}")
//    public void listenDLTForInventory(InventoryEvent inventoryEvent, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic, @Header(KafkaHeaders.OFFSET) long offset) {
//        LOGGER.info("Event on dlt topic={}, message={}", topic, inventoryEvent);
//    }

}
