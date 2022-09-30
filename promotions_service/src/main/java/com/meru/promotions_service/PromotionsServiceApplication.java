package com.meru.promotions_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableEurekaClient
@EnableMongoRepositories
public class PromotionsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PromotionsServiceApplication.class, args);
	}

}
