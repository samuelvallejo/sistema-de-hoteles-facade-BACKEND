package com.hotel.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record Invoice(UUID id, UUID reservationId, Instant issuedAt, String currency,
                      List<InvoiceLine> lines, BigDecimal subtotal, BigDecimal total) {
    public Invoice { lines = List.copyOf(lines); }

    public static Builder builder(UUID reservationId) { return new Builder(reservationId); }

    public static final class Builder {
        private final UUID reservationId;
        private final List<InvoiceLine> lines = new ArrayList<>();

        private Builder(UUID reservationId) { this.reservationId = reservationId; }

        public Builder line(String description, long quantity, BigDecimal unitPrice) {
            lines.add(new InvoiceLine(description, quantity, unitPrice,
                    unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2)));
            return this;
        }

        public Invoice build() {
            BigDecimal total = lines.stream().map(InvoiceLine::total).reduce(new BigDecimal("0.00"), BigDecimal::add);
            return new Invoice(UUID.randomUUID(), reservationId, Instant.now(), "USD", lines, total, total);
        }
    }
}
