package com.shoppix.cart_service_reactive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
@EnableDiscoveryClient
@EnableReactiveMongoRepositories
public class CartServiceReactiveApplication {

	public static void main(String[] args) {
		SpringApplication.run(CartServiceReactiveApplication.class, args);
	}

	@Bean
	@LoadBalanced
	public WebClient.Builder webClientBuilder() {
		return WebClient.builder();
	}

//	@Bean
//	public ServerCodecConfigurer serverCodecConfigurer() {
//		return ServerCodecConfigurer.create();
//	}
	
//	@Bean
//	public Docket productApi(){
//		return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.
//				basePackage("com.meru.cart_service.controller")).build();
//	}
}
