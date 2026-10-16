package com.hotel;

import com.hotel.dto.*;
import com.hotel.exception.HotelException;
import com.hotel.facade.HotelFacade;
import com.hotel.factory.*;
import com.hotel.model.*;
import com.hotel.service.*;
import com.hotel.strategy.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConcurrencyTest {
    @Test
    void onlyOneConcurrentReservationCanAcquireTheSameRoom() throws Exception {
        HotelFacade facade = new HotelFacade(new RoomService(new RoomFactory()),
                new RateService(List.of(new HighSeasonPricingStrategy(), new LowSeasonPricingStrategy())),
                new AdditionalServiceService(new AdditionalServiceFactory()), new BillingService(), new AccessService());
        ReservationRequest request = new ReservationRequest(1, LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 3),
                Season.LOW, new GuestRequest("Ana", "123", "ana@example.com", "3001234567"));
        ExecutorService executor = Executors.newFixedThreadPool(8);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> attempts = new ArrayList<>();
        try {
            for (int i = 0; i < 20; i++) attempts.add(executor.submit(() -> {
                start.await();
                try { facade.createReservation(request); return true; }
                catch (HotelException e) {
                    assertThat(e.code()).isEqualTo("ROOM_UNAVAILABLE");
                    return false;
                }
            }));
            start.countDown();
            int successes = 0;
            for (Future<Boolean> attempt : attempts) if (attempt.get(10, TimeUnit.SECONDS)) successes++;
            assertThat(successes).isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }
}
