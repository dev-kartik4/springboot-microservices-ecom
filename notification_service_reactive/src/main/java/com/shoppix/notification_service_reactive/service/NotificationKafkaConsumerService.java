package com.shoppix.notification_service_reactive.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppix.notification_service_reactive.entity.EmailRequestDto;
import com.shoppix.notification_service_reactive.events.CustomerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.shoppix.notification_service_reactive.enums.CustomerNotificationEnum.CUSTOMER_DELETED;
import static com.shoppix.notification_service_reactive.enums.CustomerNotificationEnum.CUSTOMER_REGISTERED;


@Service
@Component
public class NotificationKafkaConsumerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationKafkaConsumerService.class);

    private final ObjectMapper objectMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private final NotificationKafkaProducerService notificationKafkaProducerService;

    @Autowired
    public NotificationKafkaConsumerService(ObjectMapper objectMapper, NotificationService notificationService, NotificationKafkaProducerService notificationKafkaProducerService) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
        this.notificationKafkaProducerService = notificationKafkaProducerService;
    }

    @Retryable(
            value = { Exception.class },
            maxAttempts = 5,
            backoff = @Backoff(delay = 1000, maxDelay = 3000)
    )
    @KafkaListener(topics = "${spring.kafka.topic.customer-notification-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeMessagesFromCustomer(String message) throws JsonProcessingException {
        LOGGER.info("Received Message: {}", message);
        CustomerEvent customerEvent = objectMapper.readValue(message, CustomerEvent.class);

        try {

            EmailRequestDto emailRequest = new EmailRequestDto();
            Map<String, String> model = new HashMap<>();
            LOGGER.info("Processed CustomerEvent: {}", customerEvent);

            if(customerEvent.getCustomerMessageType().equals(CUSTOMER_REGISTERED.name())) {
                emailRequest.setTo(customerEvent.getCustomerEmail());
                emailRequest.setSubject("We are happy to have you with us!");
                model.put("name", customerEvent.getCustomerName());
                model.put("value", "Welcome to Shoppix! Have a delightful online shopping experience");
                notificationService.sendMail(emailRequest, model);
            } else if(customerEvent.getCustomerMessageType().equals(CUSTOMER_DELETED.name())){
                emailRequest.setTo(customerEvent.getCustomerEmail());
                emailRequest.setSubject("We are very sad that you are leaving us!");
                model.put("name", customerEvent.getCustomerName());
                model.put("value", "Though you are leaving us! But don't miss out on exciting offers from us");
                notificationService.sendMail(emailRequest, model);
            }

        } catch (Exception e) {
            LOGGER.error("Error processing notification event", e);
        }
    }


    @DltHandler
    @KafkaListener(topics = "${spring.kafka.dead-letter.customer-notification-dlt}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenDLT(String topic,String message) {
        LOGGER.info("Event on dlt topic={}, message={}", topic,message);
    }
}
