package com.hotel.model;

import java.math.BigDecimal;

public record Room(int id, String number, RoomType type, BigDecimal baseRate) {}
