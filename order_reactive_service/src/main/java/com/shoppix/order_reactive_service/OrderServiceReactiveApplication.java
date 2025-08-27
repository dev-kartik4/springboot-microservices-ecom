package com.shoppix.order_reactive_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
@EnableDiscoveryClient
@EnableReactiveMongoRepositories
public class OrderServiceReactiveApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderServiceReactiveApplication.class, args);
	}

	@Bean
	@LoadBalanced
	public WebClient.Builder webClientBuilder() {
		return WebClient.builder();
	}

	@Bean
	public ServerCodecConfigurer serverCodecConfigurer() {
		return ServerCodecConfigurer.create();
	}
}
