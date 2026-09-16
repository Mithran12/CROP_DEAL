package com.cropdeal.user.dto.response;

import java.time.LocalDateTime;

import com.cropdeal.user.entity.UserStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DealerResponse {

    private Long dealerId;
    private Long authUserId;
    private String businessName;
    private String contactPersonName;
    private String email;
    private String phone;
    private String businessType;
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