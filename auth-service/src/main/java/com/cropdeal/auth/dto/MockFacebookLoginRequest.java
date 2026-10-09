package com.cropdeal.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class MockFacebookLoginRequest {

    @NotBlank
    private String accountId;

    public MockFacebookLoginRequest() {
    }

    public MockFacebookLoginRequest(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
}
