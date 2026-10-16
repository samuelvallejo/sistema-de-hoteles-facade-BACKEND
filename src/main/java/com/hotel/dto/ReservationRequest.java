package com.hotel.dto;

import com.hotel.model.Season;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record ReservationRequest(
        @NotNull(message = "La habitación es obligatoria.") @Positive(message = "La habitación debe ser positiva.") Integer roomId,
        @NotNull(message = "La fecha de entrada es obligatoria.") LocalDate checkInDate,
        @NotNull(message = "La fecha de salida es obligatoria.") LocalDate checkOutDate,
        @NotNull(message = "La temporada es obligatoria.") Season season,
        @NotNull(message = "Los datos del huésped son obligatorios.") @Valid GuestRequest guest) {}
