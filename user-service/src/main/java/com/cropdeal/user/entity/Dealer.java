package com.cropdeal.user.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "dealers",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_dealer_auth_user", columnNames = "auth_user_id"),
        @UniqueConstraint(name = "uk_dealer_email", columnNames = "email")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dealer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dealerId;

    @Column(name = "auth_user_id", nullable = false, unique = true)
    private Long authUserId;

    @Column(nullable = false, length = 150)
    private String businessName;

    @Column(nullable = false, length = 100)
    private String contactPersonName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(length = 100)
    private String businessType;

    @Column(nullable = false, length = 250)
    private String addressLine;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(nullable = false, length = 10)
    private String pincode;

    @Column(nullable = false, length = 30)
    private String bankAccountNumber;

    @Column(nullable = false, length = 20)
    private String ifscCode;

    @Column(length = 100)
    private String bankName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}