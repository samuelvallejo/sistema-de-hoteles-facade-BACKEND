package com.hotel.controller;

import com.hotel.dto.*;
import com.hotel.dto.HotelResponses.*;
import com.hotel.facade.HotelFacade;
import com.hotel.model.*;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hotel")
public class HotelController {
    private final HotelFacade facade;
    public HotelController(HotelFacade facade) { this.facade = facade; }

    @PostMapping("/reservar")
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(@Valid @RequestBody ReservationRequest request) {
        ReservationResponse reservation = facade.createReservation(request);
        return ResponseEntity.created(URI.create("/api/hotel/reserva/" + reservation.id()))
                .body(new ApiResponse<>("Reserva creada correctamente.", reservation));
    }

    @GetMapping("/disponibilidad")
    public ApiResponse<List<AvailabilityResponse>> availability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam(required = false) RoomType roomType,
            @RequestParam(defaultValue = "LOW") Season season) {
        return new ApiResponse<>("Disponibilidad consultada correctamente.", facade.availability(checkInDate, checkOutDate, roomType, season));
    }

    @PostMapping("/servicios/{reservationId}")
    public ApiResponse<ReservationResponse> addService(@PathVariable UUID reservationId, @Valid @RequestBody ServiceRequest request) {
        return new ApiResponse<>("Servicio agregado correctamente.", facade.addService(reservationId, request.serviceType()));
    }

    @PutMapping("/checkin/{reservationId}")
    public ApiResponse<ReservationResponse> performCheckIn(@PathVariable UUID reservationId) {
        return new ApiResponse<>("Check-in realizado. Tu llave digital está disponible.", facade.performCheckIn(reservationId));
    }

    @PutMapping("/checkout/{reservationId}")
    public ApiResponse<ReservationResponse> performCheckOut(@PathVariable UUID reservationId) {
        return new ApiResponse<>("Check-out realizado. Factura final generada.", facade.performCheckOut(reservationId));
    }

    @GetMapping("/reserva/{reservationId}")
    public ApiResponse<ReservationResponse> getReservation(@PathVariable UUID reservationId) {
        return new ApiResponse<>("Reserva consultada correctamente.", facade.getReservation(reservationId));
    }
}
