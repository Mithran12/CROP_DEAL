package com.cropdeal.crop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cropdeal.crop.entity.Crop;
import com.cropdeal.crop.entity.CropStatus;

public interface CropRepository extends JpaRepository<Crop, Long> {

    List<Crop> findByFarmerId(Long farmerId);

    List<Crop> findByStatus(CropStatus status);

    List<Crop> findByCropNameContainingIgnoreCase(String cropName);

    List<Crop> findByCityIgnoreCase(String city);

    List<Crop> findByCategory(
            com.cropdeal.crop.entity.CropCategory category);

    Optional<Crop> findByCropIdAndFarmerId(
            Long cropId,
            Long farmerId);
}