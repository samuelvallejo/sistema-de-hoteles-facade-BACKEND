package com.hotel.service;

import com.hotel.exception.HotelException;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;

public final class StayRules {
    private StayRules() {}

    public static void validate(LocalDate start, LocalDate end) {
        if (start == null || end == null || !end.isAfter(start)) {
            throw new HotelException(HttpStatus.BAD_REQUEST, "INVALID_DATES", "La fecha de salida debe ser posterior a la fecha de entrada.");
        }
    }
}
