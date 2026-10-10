package com.cropdeal.pricing.dto;

import java.time.LocalDateTime;

public class MarketRateResponse {

	private Long id;
	private String cropName;
	private String category;
	private String state;
	private String district;
	private Double marketPrice;
	private Double maxPrice;
	private String imageUrl;
	private String location;
	private LocalDateTime updatedAt;

	// Optional legacy fields for backward compatibility
	private Double minPrice;
	private Double modalPrice;
	private Double pricePerQuintal;

	public MarketRateResponse() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCropName() {
		return cropName;
	}

	public void setCropName(String cropName) {
		this.cropName = cropName;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public Double getMarketPrice() {
		return marketPrice;
	}

	public void setMarketPrice(Double marketPrice) {
		this.marketPrice = marketPrice;
	}

	public Double getMaxPrice() {
		return maxPrice;
	}

	public void setMaxPrice(Double maxPrice) {
		this.maxPrice = maxPrice;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Double getMinPrice() {
		return minPrice != null ? minPrice : marketPrice;
	}

	public void setMinPrice(Double minPrice) {
		this.minPrice = minPrice;
	}

	public Double getModalPrice() {
		return modalPrice != null ? modalPrice : marketPrice;
	}

	public void setModalPrice(Double modalPrice) {
		this.modalPrice = modalPrice;
	}

	public Double getPricePerQuintal() {
		return pricePerQuintal != null ? pricePerQuintal : marketPrice;
	}

	public void setPricePerQuintal(Double pricePerQuintal) {
		this.pricePerQuintal = pricePerQuintal;
	}
}
