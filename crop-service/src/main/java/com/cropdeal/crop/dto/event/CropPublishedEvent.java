package com.cropdeal.crop.dto.event;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CropPublishedEvent {

    private Long cropId;

    private Long farmerId;

    private String cropName;

    private String category;

    private Double quantity;

    private String quantityUnit;

    private BigDecimal expectedPrice;

    private String quality;

    private String city;

    private String state;
}