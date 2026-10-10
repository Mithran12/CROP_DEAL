package com.cropdeal.review.security;

import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
	private SecurityUtils() {
	}

	public static Long authId() {
		return (Long) SecurityContextHolder.getContext().getAuthentication().getDetails();
	}

	public static boolean admin() {
		return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
	}
}
