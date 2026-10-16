package com.hotel.service;

import com.hotel.model.*;
import com.hotel.strategy.PricingStrategy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class RateService {
    private final Map<Season, PricingStrategy> strategies = new EnumMap<>(Season.class);

    public RateService(List<PricingStrategy> strategies) {
        strategies.forEach(strategy -> this.strategies.put(strategy.season(), strategy));
    }

    public RateQuote quote(Room room, LocalDate start, LocalDate end, Season season) {
        StayRules.validate(start, end);
        long nights = ChronoUnit.DAYS.between(start, end);
        BigDecimal nightly = strategies.get(season).nightlyRate(room.baseRate());
        BigDecimal base = room.baseRate().multiply(BigDecimal.valueOf(nights));
        BigDecimal total = nightly.multiply(BigDecimal.valueOf(nights));
        return new RateQuote(nights, season, room.baseRate(), nightly, base, total.subtract(base), total);
    }
}
