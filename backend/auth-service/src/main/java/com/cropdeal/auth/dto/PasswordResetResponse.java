package com.cropdeal.auth.dto;

public class PasswordResetResponse {

    private String message;
    private String resetToken;
    private String resetLink;

    public PasswordResetResponse() {
    }

    public PasswordResetResponse(String message) {
        this.message = message;
    }

    public PasswordResetResponse(String message, String resetToken, String resetLink) {
        this.message = message;
        this.resetToken = resetToken;
        this.resetLink = resetLink;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public String getResetLink() {
        return resetLink;
    }

    public void setResetLink(String resetLink) {
        this.resetLink = resetLink;
    }
}
