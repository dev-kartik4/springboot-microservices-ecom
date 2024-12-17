package com.shoppix.product_reactive_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ProductKafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    @Value("${spring.kafka.topic.product-notification-topic}")
    private String productNotificationTopic;

    @Value("${spring.kafka.topic.inventory-topic}")
    private String inventoryTopic;

    @Value("${spring.kafka.topic.offers-topic}")
    private String offersTopic;

    @Value("${spring.kafka.dead-letter.product-notification-dlt}")
    private String deadLetterProductNotificationTopic;

    @Value("${spring.kafka.dead-letter.inventory-dlt}")
    private String deadLetterInventoryTopic;

    @Value("${spring.kafka.dead-letter.offers-dlt}")
    private String deadLetterOffersTopic;

    @Bean
    public ProducerFactory<String,String> producerFactory(){
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public NewTopic productTopic() {
        return TopicBuilder
                .name(productNotificationTopic)
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic inventoryTopic() {
        return TopicBuilder
                .name(inventoryTopic)
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic offersTopic() {
        return TopicBuilder
                .name(offersTopic)
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic deadLetterProductTopic(){
        return TopicBuilder
                .name(deadLetterProductNotificationTopic)
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic deadLetterInventoryTopic(){
        return TopicBuilder
                .name(deadLetterInventoryTopic)
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic deadLetterOffersTopic(){
        return TopicBuilder
                .name(deadLetterOffersTopic)
                .partitions(3)
                .replicas(3)
                .build();
    }
}
