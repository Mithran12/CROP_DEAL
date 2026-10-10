package com.cropdeal.user.dto;

import com.cropdeal.user.entity.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserRequest {

	@NotNull(message = "Auth user id is required")
	private Long authUserId;

	@NotBlank(message = "Name is required")
	@Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
	private String name;

	@NotBlank(message = "Email is required")
	@Email(message = "Enter a valid email address")
	private String email;

	@NotBlank(message = "Phone number is required")
	@Pattern(regexp = "^[6-9][0-9]{9}$", message = "Phone number must be a valid 10 digit Indian mobile number")
	private String phone;

	@NotBlank(message = "Address is required")
	@Size(min = 5, max = 150, message = "Address must be between 5 and 150 characters")
	private String address;

	@NotBlank(message = "City is required")
	private String city;

	@NotBlank(message = "State is required")
	private String state;

	@Pattern(regexp = "^$|^[1-9][0-9]{5}$", message = "Pincode must be exactly 6 digits")
	private String pincode;

	@NotNull(message = "Role is required")
	private Role role;

	@Size(max = 100, message = "Account holder name is too long")
	private String accountHolderName;

	@Pattern(regexp = "^[0-9]{15}$", message = "Bank account number must be exactly 15 digits")
	private String bankAccountNumber;

	@Size(max = 100, message = "Bank name is too long")
	private String bankName;

	@Size(max = 100, message = "Branch name is too long")
	private String branch;

	@Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Enter a valid IFSC code")
	private String ifscCode;

	@Size(max = 100, message = "UPI ID is too long")
	private String upiId;

	public String getUpiId() {
		return upiId;
	}

	public void setUpiId(String upiId) {
		this.upiId = upiId;
	}

	public Long getAuthUserId() {
		return authUserId;
	}

	public void setAuthUserId(Long authUserId) {
		this.authUserId = authUserId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public String getBankAccountNumber() {
		return bankAccountNumber;
	}

	public void setBankAccountNumber(String bankAccountNumber) {
		this.bankAccountNumber = bankAccountNumber;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getIfscCode() {
		return ifscCode;
	}

	public void setIfscCode(String ifscCode) {
		this.ifscCode = ifscCode;
	}

	public String getPincode() {
		return pincode;
	}

	public void setPincode(String pincode) {
		this.pincode = pincode;
	}

	public String getAccountHolderName() {
		return accountHolderName;
	}

	public void setAccountHolderName(String accountHolderName) {
		this.accountHolderName = accountHolderName;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

}