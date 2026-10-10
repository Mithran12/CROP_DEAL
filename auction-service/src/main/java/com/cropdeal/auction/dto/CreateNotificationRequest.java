package com.cropdeal.auction.dto;

public class CreateNotificationRequest {

	private Long userId;
	private String type;
	private String message;

	public CreateNotificationRequest() {
	}

	public CreateNotificationRequest(Long userId, String type, String message) {
		this.userId = userId;
		this.type = type;
		this.message = message;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
