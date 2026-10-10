package com.cropdeal.pricing.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_rates")
public class MarketRate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String cropName;

	private String category;

	private String state;

	private String district;

	private String location;

	private Double minPrice;

	private Double maxPrice;

	private Double modalPrice;

	private Double pricePerQuintal;

	private Double marketPrice;

	@Lob
	@Column(columnDefinition = "LONGTEXT")
	private String imageUrl;

	private LocalDateTime updatedAt;

	public MarketRate() {
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

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Double getMinPrice() {
		return minPrice;
	}

	public void setMinPrice(Double minPrice) {
		this.minPrice = minPrice;
	}

	public Double getMaxPrice() {
		return maxPrice;
	}

	public void setMaxPrice(Double maxPrice) {
		this.maxPrice = maxPrice;
	}

	public Double getModalPrice() {
		return modalPrice;
	}

	public void setModalPrice(Double modalPrice) {
		this.modalPrice = modalPrice;
	}

	public Double getPricePerQuintal() {
		return pricePerQuintal;
	}

	public void setPricePerQuintal(Double pricePerQuintal) {
		this.pricePerQuintal = pricePerQuintal;
		if (this.marketPrice == null) {
			this.marketPrice = pricePerQuintal;
		}
	}

	public Double getMarketPrice() {
		return marketPrice;
	}

	public void setMarketPrice(Double marketPrice) {
		this.marketPrice = marketPrice;
		if (this.pricePerQuintal == null) {
			this.pricePerQuintal = marketPrice;
		}
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}