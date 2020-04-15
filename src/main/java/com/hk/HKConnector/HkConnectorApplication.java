package com.hk.HKConnector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.*;
import org.springframework.context.annotation.*;
import org.springframework.web.client.*;

@SpringBootApplication
public class HkConnectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(HkConnectorApplication.class, args);
	}

	//TODO : Move to configurations
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}
}
