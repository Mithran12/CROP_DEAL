package com.cropdeal.pricing.config;

import com.cropdeal.pricing.entity.MarketRate;
import com.cropdeal.pricing.repository.MarketRateRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class MarketRateInitializer implements ApplicationRunner {

    private final MarketRateRepository repository;

    public MarketRateInitializer(MarketRateRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) {
        
    }
}

