package com.cropdeal.pricing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class MarketRateRequest {

	@NotBlank(message = "Crop name is required")
	private String cropName;

	@NotBlank(message = "Category is required")
	private String category;

	@NotBlank(message = "State is required")
	private String state;

	@NotBlank(message = "District/Market is required")
	private String district;

	@NotNull(message = "Market price is required")
	@PositiveOrZero(message = "Market price cannot be negative")
	private Double marketPrice;

	@NotNull(message = "Maximum price is required")
	@PositiveOrZero(message = "Maximum price cannot be negative")
	private Double maxPrice;

	private String imageUrl;

	// Optional legacy fields for backward compatibility
	private Double minPrice;
	private Double modalPrice;
	private Double pricePerQuintal;

	public MarketRateRequest() {
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
		return marketPrice != null ? marketPrice : pricePerQuintal;
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
