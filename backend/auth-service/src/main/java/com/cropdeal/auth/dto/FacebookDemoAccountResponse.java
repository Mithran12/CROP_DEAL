package com.cropdeal.auth.dto;

public class FacebookDemoAccountResponse {

	private String accountId;
	private String displayName;
	private String email;
	private String role;

	public FacebookDemoAccountResponse() {
	}

	public FacebookDemoAccountResponse(String accountId, String displayName, String email, String role) {
		this.accountId = accountId;
		this.displayName = displayName;
		this.email = email;
		this.role = role;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
}
