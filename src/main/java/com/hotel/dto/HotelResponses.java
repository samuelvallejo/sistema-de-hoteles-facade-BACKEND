package com.hotel.dto;

import com.hotel.model.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class HotelResponses {
    private HotelResponses() {}

    public record RoomResponse(int id, String number, RoomType type, BigDecimal baseRate) {}
    public record GuestResponse(String name, String document, String email, String phone) {}
    public record RateResponse(long nights, Season season, BigDecimal baseNightlyRate, BigDecimal nightlyRate,
                               BigDecimal baseTotal, BigDecimal seasonSurcharge, BigDecimal total) {}
    public record ServiceResponse(ServiceType type, String description, long quantity, BigDecimal unitPrice, BigDecimal total) {}
    public record InvoiceLineResponse(String description, long quantity, BigDecimal unitPrice, BigDecimal total) {}
    public record InvoiceResponse(UUID id, UUID reservationId, Instant issuedAt, String currency,
                                  List<InvoiceLineResponse> lines, BigDecimal subtotal, BigDecimal total) {}
    public record AvailabilityResponse(RoomResponse room, RateResponse rate) {}
    public record ReservationResponse(UUID id, GuestResponse guest, RoomResponse room, LocalDate checkInDate,
                                      LocalDate checkOutDate, RateResponse rate, ReservationStatus status,
                                      List<ServiceResponse> services, BigDecimal servicesTotal, BigDecimal total,
                                      UUID digitalKey, InvoiceResponse invoice) {}

    public static RoomResponse room(Room r) { return new RoomResponse(r.id(), r.number(), r.type(), r.baseRate()); }

    public static RateResponse rate(RateQuote r) {
        return new RateResponse(r.nights(), r.season(), r.baseNightlyRate(), r.nightlyRate(),
                r.baseTotal(), r.seasonSurcharge(), r.total());
    }

    public static ReservationResponse reservation(Reservation r) {
        List<ServiceResponse> services = r.services().stream().map(s ->
                new ServiceResponse(s.type(), s.description(), s.quantity(), s.unitPrice(), s.total())).toList();
        BigDecimal servicesTotal = services.stream().map(ServiceResponse::total).reduce(new BigDecimal("0.00"), BigDecimal::add);
        Invoice i = r.invoice();
        InvoiceResponse invoice = i == null ? null : new InvoiceResponse(i.id(), i.reservationId(), i.issuedAt(),
                i.currency(), i.lines().stream().map(l -> new InvoiceLineResponse(l.description(), l.quantity(), l.unitPrice(), l.total())).toList(),
                i.subtotal(), i.total());
        Guest g = r.guest();
        return new ReservationResponse(r.id(), new GuestResponse(g.name(), g.document(), g.email(), g.phone()),
                room(r.room()), r.checkInDate(), r.checkOutDate(), rate(r.rate()), r.status(), services,
                servicesTotal, r.rate().total().add(servicesTotal), r.digitalKey(), invoice);
    }
}
