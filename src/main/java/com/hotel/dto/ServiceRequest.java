package com.hotel.dto;

import com.hotel.model.ServiceType;
import jakarta.validation.constraints.NotNull;

public record ServiceRequest(@NotNull(message = "El tipo de servicio es obligatorio.") ServiceType serviceType) {}
