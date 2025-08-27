package com.shoppix.notification_service_reactive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootApplication
@EnableDiscoveryClient
public class NotificationServiceReactiveApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationServiceReactiveApplication.class, args);
	}

}
