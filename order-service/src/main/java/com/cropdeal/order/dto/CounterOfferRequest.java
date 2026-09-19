package com.cropdeal.order.dto;
import jakarta.validation.constraints.NotNull; import jakarta.validation.constraints.Positive; import jakarta.validation.constraints.Size;
public class CounterOfferRequest { @NotNull @Positive private Double offeredPrice; @Size(max=1000) private String message; public Double getOfferedPrice(){return offeredPrice;} public void setOfferedPrice(Double v){offeredPrice=v;} public String getMessage(){return message;} public void setMessage(String v){message=v;} }
