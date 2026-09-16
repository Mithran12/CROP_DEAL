package com.cropdeal.user.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.cropdeal.user.entity.UserStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FarmerResponse {

    private Long farmerId;
    private Long authUserId;
    private String fullName;
    private String email;
    private String phone;
    private String farmName;
    private BigDecimal farmSize;
    private String farmSizeUnit;
    private String addressLine;
    private String city;
    private String state;
    private String pincode;
    private String bankAccountNumber;
    private String ifscCode;
    private String bankName;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}