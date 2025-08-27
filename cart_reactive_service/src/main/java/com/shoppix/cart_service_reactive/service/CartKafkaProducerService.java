package com.shoppix.cart_service_reactive.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Component
public class CartKafkaProducerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CartKafkaProducerService.class);
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public CartKafkaProducerService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topicName,String message) {


        CompletableFuture<SendResult<String, String>> futureResponse = kafkaTemplate.send(topicName, message);
        futureResponse.whenCompleteAsync((result,exception) -> {
            if(exception == null){
                LOGGER.info("Sent Message=[" + message +
                        "] with offset=[" + result.getRecordMetadata().offset() + "]");
            }else{
                LOGGER.error("Unable to Send Message=[" + message + "] due to: " + exception.getMessage());
            }
        });
    }
}
