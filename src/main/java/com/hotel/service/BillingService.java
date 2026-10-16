package com.hotel.service;

import com.hotel.model.*;
import org.springframework.stereotype.Service;

@Service
public class BillingService {
    public Invoice generate(Reservation reservation) {
        RateQuote rate = reservation.rate();
        Invoice.Builder builder = Invoice.builder(reservation.id())
                .line("Alojamiento por noche - habitación " + reservation.room().number(), rate.nights(), rate.baseNightlyRate());
        if (rate.season() == Season.HIGH) {
            builder.line("Recargo de temporada alta por noche (50 %)", rate.nights(), rate.nightlyRate().subtract(rate.baseNightlyRate()));
        }
        reservation.services().forEach(s -> builder.line(s.description(), s.quantity(), s.unitPrice()));
        return builder.build();
    }
}
