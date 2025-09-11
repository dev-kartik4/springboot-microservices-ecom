package com.eserver.eureka_reactive_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaReactiveServiceApplication {

	public static void main(String[] args) {
        System.out.println("HEY BUDDY EUREKA IS UP ! CHEERS");
		SpringApplication.run(EurekaReactiveServiceApplication.class, args);
	}

}
