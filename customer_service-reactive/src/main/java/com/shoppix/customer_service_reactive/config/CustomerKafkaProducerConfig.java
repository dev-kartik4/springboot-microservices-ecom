package com.shoppix.customer_service_reactive.config;

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
public class CustomerKafkaProducerConfig {

    @Value("${spring.kafka.topic.cart-topic}")
    private String cartTopic;

    @Value("${spring.kafka.topic.order-topic}")
    private String orderTopic;

    @Value("${spring.kafka.topic.customer-notification-topic}")
    private String customerNotificationTopic;

    @Value("${spring.kafka.dead-letter.cart-dlt}")
    private String deadLetterCartTopic;

    @Value("${spring.kafka.dead-letter.order-dlt}")
    private String deadLetterOrderTopic;

    @Value("${spring.kafka.dead-letter.customer-notification-dlt}")
    private String deadLetterCustomerNotificationTopic;

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    @Bean
    public ProducerFactory<String,String> producerFactory(){
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public NewTopic cartTopic(){
        return TopicBuilder
                .name(cartTopic)
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic orderTopic(){
        return TopicBuilder
                .name(orderTopic)
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic deadLetterCartTopic(){
        return TopicBuilder
                .name(deadLetterCartTopic)
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic deadLetterOrderTopic(){
        return TopicBuilder
                .name(deadLetterOrderTopic)
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic notificationTopic(){
        return TopicBuilder
                .name(customerNotificationTopic)
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic deadLetterNotificationTopic(){
        return TopicBuilder
                .name(deadLetterCustomerNotificationTopic)
                .partitions(3)
                .replicas(3)
                .build();
    }
}