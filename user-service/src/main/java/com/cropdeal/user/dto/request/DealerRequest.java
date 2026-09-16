package com.cropdeal.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class DealerRequest {

    @NotNull(message = "Auth user ID is required")
    private Long authUserId;

    @NotBlank(message = "Business name is required")
    @Size(max = 150, message = "Business name cannot exceed 150 characters")
    private String businessName;

    @NotBlank(message = "Contact person name is required")
    private String contactPersonName;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 characters")
    private String phone;

    private String businessType;

    @NotBlank(message = "Address is required")
    private String addressLine;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Pincode is required")
    @Size(min = 6, max = 6, message = "Pincode must contain 6 digits")
    private String pincode;

    @NotBlank(message = "Bank account number is required")
    private String bankAccountNumber;

    @NotBlank(message = "IFSC code is required")
    private String ifscCode;

    private String bankName;
}