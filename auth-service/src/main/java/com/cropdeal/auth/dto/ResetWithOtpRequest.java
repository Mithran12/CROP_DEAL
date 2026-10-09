package com.cropdeal.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ResetWithOtpRequest {

	@NotBlank(message = "Please enter your email")
	@Email(message = "Please enter a valid email address")
	private String email;

	@NotBlank(message = "Please enter the OTP")
	@Pattern(regexp = "^[0-9]{6}$", message = "OTP must be exactly 6 digits")
	private String otp;

	@NotBlank(message = "Please enter a new password")
	@Size(min = 6, max = 50, message = "Password must be at least 6 characters")
	private String newPassword;

	@NotBlank(message = "Please confirm your new password")
	private String confirmPassword;

	public ResetWithOtpRequest() {
	}

	public ResetWithOtpRequest(String email, String otp, String newPassword, String confirmPassword) {
		this.email = email;
		this.otp = otp;
		this.newPassword = newPassword;
		this.confirmPassword = confirmPassword;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}
}
