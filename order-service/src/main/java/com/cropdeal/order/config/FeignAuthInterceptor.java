package com.cropdeal.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

@Component
public class FeignAuthInterceptor implements RequestInterceptor {

	@Override
	public void apply(RequestTemplate template) {

		String authorization = org.springframework.web.context.request.RequestContextHolder
				.getRequestAttributes() != null
						? ((jakarta.servlet.http.HttpServletRequest) org.springframework.web.context.request.RequestContextHolder
								.currentRequestAttributes().resolveReference(
										org.springframework.web.context.request.RequestAttributes.REFERENCE_REQUEST))
								.getHeader("Authorization")
						: null;

		if (authorization != null && !authorization.isBlank()) {
			template.header("Authorization", authorization);
		}
	}
}