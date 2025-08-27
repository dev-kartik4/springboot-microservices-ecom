package com.shoppix.notification_reactive_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class NotificationServiceReactiveApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationServiceReactiveApplication.class, args);
	}

}
