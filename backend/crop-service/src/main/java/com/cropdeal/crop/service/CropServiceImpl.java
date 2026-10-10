package com.cropdeal.crop.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.cropdeal.crop.client.UserServiceClient;
import com.cropdeal.crop.dto.CropRequest;
import com.cropdeal.crop.dto.CropResponse;
import com.cropdeal.crop.dto.UserResponse;
import com.cropdeal.crop.entity.Crop;
import com.cropdeal.crop.entity.CropStatus;
import com.cropdeal.crop.exception.CropNotFoundException;
import com.cropdeal.crop.messaging.CropEventProducer;
import com.cropdeal.crop.messaging.CropPublishedEvent;
import com.cropdeal.crop.repository.CropRepository;
import com.cropdeal.crop.security.SecurityUtils;

@Service
public class CropServiceImpl implements CropService {

	private final CropRepository cropRepository;
	private final UserServiceClient userServiceClient;
	private final CropEventProducer cropEventProducer;

	public CropServiceImpl(CropRepository cropRepository, UserServiceClient userServiceClient,
			CropEventProducer cropEventProducer) {

		this.cropRepository = cropRepository;
		this.userServiceClient = userServiceClient;
		this.cropEventProducer = cropEventProducer;
	}

	@Override
	public CropResponse createCrop(CropRequest request) {

		UserResponse user = userServiceClient.getFarmer(request.getFarmerId());

		if (!SecurityUtils.getCurrentAuthUserId().equals(user.getAuthUserId())) {
			throw new AccessDeniedException("You can publish crops only for your own farmer profile");
		}

		if (!"FARMER".equalsIgnoreCase(user.getRole())) {
			throw new IllegalArgumentException("Only farmers can publish crops");
		}

		if (!user.isActive()) {
			throw new IllegalArgumentException("Farmer account is not active");
		}

		validateGrain(request);

		Crop crop = new Crop();

		crop.setFarmerId(request.getFarmerId());
		crop.setCategory(request.getCategory());
		crop.setCropName(request.getCropName());
		crop.setGrade(request.getGrade().toUpperCase());
		crop.setQuantity(request.getQuantity());
		crop.setExpectedPrice(request.getExpectedPrice());
		crop.setLocation(request.getLocation());
		crop.setImageUrl(request.getImageUrl());
		crop.setStatus(request.getQuantity() != null && request.getQuantity() > 0 ? CropStatus.AVAILABLE
				: CropStatus.UNAVAILABLE);
		crop.setPublishedAt(LocalDateTime.now());
		crop.setUpdatedAt(LocalDateTime.now());

		Crop savedCrop = cropRepository.save(crop);

		CropPublishedEvent event = new CropPublishedEvent();

		event.setCropId(savedCrop.getId());
		event.setFarmerId(savedCrop.getFarmerId());
		event.setCropName(savedCrop.getCropName());
		event.setCategory(savedCrop.getCategory());
		event.setGrade(savedCrop.getGrade());
		event.setLocation(savedCrop.getLocation());
		event.setQuantity(savedCrop.getQuantity());
		event.setExpectedPrice(savedCrop.getExpectedPrice());

		cropEventProducer.publishCropCreated(event);

		return mapToResponse(savedCrop);
	}

	@Override
	public CropResponse getCropById(Long id) {

		Crop crop = cropRepository.findById(id)
				.orElseThrow(() -> new CropNotFoundException("Crop not found with id: " + id));

		return mapToResponse(crop);
	}

	@Override
	public List<CropResponse> getAllCrops() {

		return cropRepository.findAll().stream().map(this::mapToResponse).toList();
	}

	@Override
	public List<CropResponse> getCropsByCategory(String category) {

		return cropRepository.findByCategoryIgnoreCase(category).stream().map(this::mapToResponse).toList();
	}

	@Override
	public List<CropResponse> searchCropsByName(String cropName) {

		return cropRepository.findByCropNameContainingIgnoreCase(cropName).stream().map(this::mapToResponse).toList();
	}

	@Override
	public List<CropResponse> filterCrops(String name, String grade, String location) {

		Specification<Crop> spec = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

		if (name != null && !name.isBlank()) {
			spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder
					.like(criteriaBuilder.lower(root.get("cropName")), "%" + name.toLowerCase() + "%"));
		}

		if (grade != null && !grade.isBlank()) {
			spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder
					.equal(criteriaBuilder.lower(root.get("grade")), grade.toLowerCase()));
		}

		if (location != null && !location.isBlank()) {
			spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder
					.equal(criteriaBuilder.lower(root.get("location")), location.toLowerCase()));
		}

		return cropRepository.findAll(spec).stream().map(this::mapToResponse).toList();
	}

	@Override
	public List<CropResponse> advancedFilter(String name, String category, String location, String grade, Long farmerId,
			CropStatus status) {

		Specification<Crop> spec = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

		if (name != null && !name.isBlank()) {
			spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder
					.like(criteriaBuilder.lower(root.get("cropName")), "%" + name.toLowerCase() + "%"));
		}

		if (category != null && !category.isBlank()) {
			spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder
					.equal(criteriaBuilder.lower(root.get("category")), category.toLowerCase()));
		}

		if (location != null && !location.isBlank()) {
			spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder
					.equal(criteriaBuilder.lower(root.get("location")), location.toLowerCase()));
		}

		if (grade != null && !grade.isBlank()) {
			spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder
					.equal(criteriaBuilder.lower(root.get("grade")), grade.toLowerCase()));
		}

		if (farmerId != null) {
			spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("farmerId"), farmerId));
		}

		if (status != null) {
			spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status));
		}

		return cropRepository.findAll(spec).stream().map(this::mapToResponse).toList();
	}

	@Override
	public List<CropResponse> getCropsByLocation(String location) {

		return cropRepository.findByLocationIgnoreCase(location).stream().map(this::mapToResponse).toList();
	}

	@Override
	public List<CropResponse> getCropsByStatus(CropStatus status) {

		return cropRepository.findByStatus(status).stream().map(this::mapToResponse).toList();
	}

	@Override
	public List<CropResponse> getCropsByFarmerId(Long farmerId) {

		return cropRepository.findByFarmerId(farmerId).stream().map(this::mapToResponse).toList();
	}

	@Override
	public CropResponse updateCrop(Long id, CropRequest request) {

		Crop crop = cropRepository.findById(id)
				.orElseThrow(() -> new CropNotFoundException("Crop not found with id: " + id));

		UserResponse user = userServiceClient.getFarmer(crop.getFarmerId());

		if (!SecurityUtils.getCurrentAuthUserId().equals(user.getAuthUserId())) {

			throw new AccessDeniedException("You can update only your own crops");
		}

		if (!"FARMER".equalsIgnoreCase(user.getRole())) {
			throw new IllegalArgumentException("Only farmers can update crops");
		}

		if (!user.isActive()) {
			throw new IllegalArgumentException("Farmer account is not active");
		}

		if (request.getFarmerId() != null && !crop.getFarmerId().equals(request.getFarmerId())) {

			throw new IllegalArgumentException("Crop owner cannot be changed");
		}

		validateGrain(request);

		crop.setCategory(request.getCategory());
		crop.setCropName(request.getCropName());
		crop.setGrade(request.getGrade().toUpperCase());
		crop.setQuantity(request.getQuantity());
		crop.setExpectedPrice(request.getExpectedPrice());
		crop.setLocation(request.getLocation());
		if (request.getImageUrl() != null) {
			crop.setImageUrl(request.getImageUrl());
		}
		crop.setUpdatedAt(LocalDateTime.now());
		if (request.getQuantity() != null && request.getQuantity() == 0) {
			crop.setStatus(CropStatus.UNAVAILABLE);
		} else if (request.getQuantity() != null && request.getQuantity() > 0
				&& crop.getStatus() == CropStatus.UNAVAILABLE) {
			crop.setStatus(CropStatus.AVAILABLE);
		}

		Crop savedCrop = cropRepository.save(crop);

		return mapToResponse(savedCrop);
	}

	@Override
	public CropResponse updateCropStatus(Long id, CropStatus status) {

		Crop crop = cropRepository.findById(id)
				.orElseThrow(() -> new CropNotFoundException("Crop not found with id: " + id));

		UserResponse user = userServiceClient.getFarmer(crop.getFarmerId());

		if (!SecurityUtils.getCurrentAuthUserId().equals(user.getAuthUserId())) {

			throw new AccessDeniedException("You can update status only for your own crops");
		}

		if (status != CropStatus.AVAILABLE && status != CropStatus.INACTIVE) {

			throw new IllegalArgumentException("Farmer can set crop status only to AVAILABLE or INACTIVE");
		}

		if (crop.getStatus() == CropStatus.SOLD || crop.getStatus() == CropStatus.EXPIRED) {

			throw new IllegalArgumentException("Sold or expired crops cannot be made available again");
		}

		crop.setStatus(status);
		crop.setUpdatedAt(LocalDateTime.now());

		return mapToResponse(cropRepository.save(crop));
	}

	@Override
	public void deleteCrop(Long id) {

		Crop crop = cropRepository.findById(id)
				.orElseThrow(() -> new CropNotFoundException("Crop not found with id: " + id));

		UserResponse user = userServiceClient.getFarmer(crop.getFarmerId());

		if (!SecurityUtils.getCurrentAuthUserId().equals(user.getAuthUserId())) {

			throw new AccessDeniedException("You can delete only your own crops");
		}

		cropRepository.delete(crop);
	}

	@Override
	public CropResponse restoreQuantity(Long cropId, Double quantity) {

		Crop crop = cropRepository.findById(cropId).orElseThrow(() -> new CropNotFoundException("Crop not found"));

		if (quantity == null || quantity <= 0) {
			throw new IllegalArgumentException("Quantity must be greater than zero");
		}

		crop.setQuantity(crop.getQuantity() + quantity);

		if ((crop.getStatus() == CropStatus.SOLD || crop.getStatus() == CropStatus.UNAVAILABLE)
				&& crop.getQuantity() > 0) {

			crop.setStatus(CropStatus.AVAILABLE);
		}

		crop.setUpdatedAt(LocalDateTime.now());

		return mapToResponse(cropRepository.save(crop));
	}

	@Override
	public CropResponse reduceQuantity(Long cropId, Double quantity) {

		Crop crop = cropRepository.findById(cropId).orElseThrow(() -> new CropNotFoundException("Crop not found"));

		if (quantity == null || quantity <= 0) {
			throw new IllegalArgumentException("Quantity must be greater than zero");
		}

		if (crop.getQuantity() < quantity) {
			throw new IllegalArgumentException("Insufficient crop quantity");
		}

		double remainingQuantity = crop.getQuantity() - quantity;

		crop.setQuantity(remainingQuantity);
		crop.setUpdatedAt(LocalDateTime.now());

		if (remainingQuantity == 0) {
			crop.setStatus(CropStatus.UNAVAILABLE);
		}

		Crop savedCrop = cropRepository.save(crop);

		return mapToResponse(savedCrop);
	}

	private CropResponse mapToResponse(Crop crop) {

		CropResponse response = new CropResponse();

		response.setId(crop.getId());
		response.setFarmerId(crop.getFarmerId());
		response.setCategory(crop.getCategory());
		response.setCropName(crop.getCropName());
		response.setGrade(crop.getGrade());
		response.setQuantity(crop.getQuantity());
		response.setExpectedPrice(crop.getExpectedPrice());
		response.setLocation(crop.getLocation());
		response.setImageUrl(crop.getImageUrl());
		response.setStatus(crop.getStatus());
		response.setPublishedAt(crop.getPublishedAt());
		response.setUpdatedAt(crop.getUpdatedAt());

		return response;
	}

	private void validateGrain(CropRequest request) {
		if (request.getCategory() != null && request.getCategory().toLowerCase().contains("grain")) {
			if (request.getQuantity() != null && request.getQuantity() > 0 && request.getQuantity() < 1.0) {
				throw new IllegalArgumentException("Grain crops require a minimum quantity lot of 1 quintal");
			}
			if (request.getExpectedPrice() != null && request.getExpectedPrice() < 100.0) {
				throw new IllegalArgumentException("Expected price for grain crops must be at least ₹100 per quintal");
			}
		}
	}
}