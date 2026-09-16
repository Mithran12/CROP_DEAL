package com.cropdeal.order.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI cropDealOrderServiceOpenAPI() {

		return new OpenAPI().info(new Info().title("CropDeal Order Service API").version("1.0.0")
				.description("REST API for CropDeal Order Service. " + "Handles purchase requests, negotiations, "
						+ "orders, order lifecycle and payment events.")
				.contact(new Contact().name("CropDeal Team")));
	}
}