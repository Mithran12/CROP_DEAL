package com.cropdeal.review.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtService jwt;

	public JwtAuthenticationFilter(JwtService j) {
		jwt = j;
	}

	protected void doFilterInternal(HttpServletRequest r, HttpServletResponse s, FilterChain c)
			throws ServletException, IOException {
		String h = r.getHeader("Authorization");
		if (h != null && h.startsWith("Bearer ")) {
			String t = h.substring(7);
			if (jwt.valid(t)) {
				String role = jwt.role(t);
				var a = new UsernamePasswordAuthenticationToken(jwt.claimsEmail(t), null,
						List.of(new SimpleGrantedAuthority("ROLE_" + role)));
				a.setDetails(jwt.userId(t));
				SecurityContextHolder.getContext().setAuthentication(a);
			}
		}
		c.doFilter(r, s);
	}
}
