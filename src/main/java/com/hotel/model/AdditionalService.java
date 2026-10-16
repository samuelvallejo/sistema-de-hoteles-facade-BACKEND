package com.hotel.model;

import java.math.BigDecimal;

public record AdditionalService(ServiceType type, String description, long quantity,
                                BigDecimal unitPrice, BigDecimal total) {}
