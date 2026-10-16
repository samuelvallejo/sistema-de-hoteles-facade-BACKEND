package com.hotel.strategy;

import com.hotel.model.Season;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class HighSeasonPricingStrategy implements PricingStrategy {
    public Season season() { return Season.HIGH; }
    public BigDecimal nightlyRate(BigDecimal baseRate) { return baseRate.multiply(new BigDecimal("1.5")).setScale(2, RoundingMode.HALF_UP); }
}
