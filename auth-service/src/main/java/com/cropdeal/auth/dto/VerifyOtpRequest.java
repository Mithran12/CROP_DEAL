package com.cropdeal.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class VerifyOtpRequest {

	@NotBlank(message = "Please enter your email")
	@Email(message = "Please enter a valid email address")
	private String email;

	@NotBlank(message = "Please enter the OTP")
	@Pattern(regexp = "^[0-9]{6}$", message = "OTP must be exactly 6 digits")
	private String otp;

	public VerifyOtpRequest() {
	}

	public VerifyOtpRequest(String email, String otp) {
		this.email = email;
		this.otp = otp;
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
}
