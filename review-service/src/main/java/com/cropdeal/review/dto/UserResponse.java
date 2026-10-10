package com.cropdeal.review.dto;

public class UserResponse {
	private Long id, authUserId;
	private String role;
	private boolean active;

	public Long getId() {
		return id;
	}

	public void setId(Long v) {
		id = v;
	}

	public Long getAuthUserId() {
		return authUserId;
	}

	public void setAuthUserId(Long v) {
		authUserId = v;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String v) {
		role = v;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean v) {
		active = v;
	}
}
