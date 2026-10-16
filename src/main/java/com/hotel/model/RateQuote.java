package com.hotel.model;

import java.math.BigDecimal;

public record RateQuote(long nights, Season season, BigDecimal baseNightlyRate,
                        BigDecimal nightlyRate, BigDecimal baseTotal,
                        BigDecimal seasonSurcharge, BigDecimal total) {}
