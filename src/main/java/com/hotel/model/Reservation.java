package com.hotel.model;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public record Reservation(UUID id, Guest guest, Room room, LocalDate checkInDate,
                          LocalDate checkOutDate, RateQuote rate, ReservationStatus status,
                          List<AdditionalService> services, UUID digitalKey, Invoice invoice) {
    public Reservation { services = List.copyOf(services); }

    public Reservation withService(AdditionalService service) {
        return new Reservation(id, guest, room, checkInDate, checkOutDate, rate, status,
                Stream.concat(services.stream(), Stream.of(service)).toList(), digitalKey, invoice);
    }

    public Reservation checkedIn(UUID key) {
        return new Reservation(id, guest, room, checkInDate, checkOutDate, rate,
                ReservationStatus.CHECKED_IN, services, key, null);
    }

    public Reservation checkedOut(Invoice finalInvoice) {
        return new Reservation(id, guest, room, checkInDate, checkOutDate, rate,
                ReservationStatus.CHECKED_OUT, services, null, finalInvoice);
    }
}
