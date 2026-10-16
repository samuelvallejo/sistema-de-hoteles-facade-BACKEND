package com.hotel.strategy;

import com.hotel.model.Season;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class LowSeasonPricingStrategy implements PricingStrategy {
    public Season season() { return Season.LOW; }
    public BigDecimal nightlyRate(BigDecimal baseRate) { return baseRate; }
}
