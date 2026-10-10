package com.cropdeal.review.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {
	@Bean
	public RequestInterceptor authForwarder() {
		return template -> {
			ServletRequestAttributes a = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			if (a != null) {
				String h = a.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
				if (h != null)
					template.header(HttpHeaders.AUTHORIZATION, h);
			}
		};
	}
}
