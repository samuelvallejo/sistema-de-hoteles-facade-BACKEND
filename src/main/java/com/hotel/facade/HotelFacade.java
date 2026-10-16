package com.hotel.facade;

import com.hotel.dto.*;
import com.hotel.dto.HotelResponses.*;
import com.hotel.exception.HotelException;
import com.hotel.model.*;
import com.hotel.service.*;
import java.time.LocalDate;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class HotelFacade {
    private final RoomService rooms;
    private final RateService rates;
    private final AdditionalServiceService additionalServices;
    private final BillingService billing;
    private final AccessService access;
    private final Map<UUID, Reservation> reservations = new HashMap<>();

    public HotelFacade(RoomService rooms, RateService rates, AdditionalServiceService additionalServices,
                       BillingService billing, AccessService access) {
        this.rooms = rooms;
        this.rates = rates;
        this.additionalServices = additionalServices;
        this.billing = billing;
        this.access = access;
    }

    public synchronized ReservationResponse createReservation(ReservationRequest request) {
        Room room = rooms.find(request.roomId());
        RateQuote rate = rates.quote(room, request.checkInDate(), request.checkOutDate(), request.season());
        UUID id = UUID.randomUUID();
        GuestRequest guest = request.guest();
        Reservation reservation = new Reservation(id, new Guest(guest.name().trim(), guest.document().trim(),
                guest.email().trim(), guest.phone().trim()), room, request.checkInDate(), request.checkOutDate(),
                rate, ReservationStatus.CONFIRMED, List.of(), null, null);
        rooms.reserve(id, room.id(), request.checkInDate(), request.checkOutDate());
        reservations.put(id, reservation);
        return HotelResponses.reservation(reservation);
    }

    public synchronized List<AvailabilityResponse> availability(LocalDate start, LocalDate end, RoomType type, Season season) {
        return rooms.available(start, end, type).stream().map(room -> new AvailabilityResponse(
                HotelResponses.room(room), HotelResponses.rate(rates.quote(room, start, end, season)))).toList();
    }

    public synchronized ReservationResponse getReservation(UUID id) { return HotelResponses.reservation(find(id)); }

    public synchronized ReservationResponse addService(UUID id, ServiceType type) {
        Reservation reservation = find(id);
        if (reservation.status() == ReservationStatus.CHECKED_OUT) {
            throw new HotelException(HttpStatus.CONFLICT, "INVALID_STATE", "No se pueden agregar servicios a una reserva finalizada.");
        }
        if (reservation.services().stream().anyMatch(s -> s.type() == type)) {
            throw new HotelException(HttpStatus.CONFLICT, "SERVICE_ALREADY_ADDED", "Este servicio ya está incluido en la reserva.");
        }
        Reservation updated = reservation.withService(additionalServices.addService(type, reservation.rate().nights()));
        reservations.put(id, updated);
        return HotelResponses.reservation(updated);
    }

    public synchronized ReservationResponse performCheckIn(UUID id) {
        Reservation reservation = find(id);
        requireState(reservation, ReservationStatus.CONFIRMED, "Solo se puede realizar check-in a una reserva confirmada.");
        Reservation updated = reservation.checkedIn(access.generateKey(id));
        reservations.put(id, updated);
        return HotelResponses.reservation(updated);
    }

    public synchronized ReservationResponse performCheckOut(UUID id) {
        Reservation reservation = find(id);
        requireState(reservation, ReservationStatus.CHECKED_IN, "Debes realizar el check-in antes del check-out; la reserva no puede estar finalizada.");
        Invoice invoice = billing.generate(reservation);
        Reservation updated = reservation.checkedOut(invoice);
        access.revokeKey(id);
        rooms.release(id);
        reservations.put(id, updated);
        return HotelResponses.reservation(updated);
    }

    private Reservation find(UUID id) {
        Reservation reservation = reservations.get(id);
        if (reservation == null) throw new HotelException(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND", "La reserva no existe.");
        return reservation;
    }

    private void requireState(Reservation reservation, ReservationStatus expected, String message) {
        if (reservation.status() != expected) throw new HotelException(HttpStatus.CONFLICT, "INVALID_STATE", message);
    }
}
