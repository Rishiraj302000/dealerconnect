package com.dealerconnect.dealer_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class DealerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DealerServiceApplication.class, args);
	}

}
