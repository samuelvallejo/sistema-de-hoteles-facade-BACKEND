package com.hotel.strategy;

import com.hotel.model.Season;
import java.math.BigDecimal;

public interface PricingStrategy {
    Season season();
    BigDecimal nightlyRate(BigDecimal baseRate);
}
