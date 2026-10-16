package com.hotel;

import com.hotel.dto.*;
import com.hotel.dto.HotelResponses.*;
import com.hotel.exception.HotelException;
import com.hotel.facade.HotelFacade;
import com.hotel.factory.*;
import com.hotel.model.*;
import com.hotel.repository.ReservationRegistry;
import com.hotel.service.*;
import com.hotel.strategy.*;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class HotelFacadeTest {
    private static final LocalDate START = LocalDate.of(2036, 7, 1);
    private static final LocalDate END = LocalDate.of(2036, 7, 4);
    private HotelFacade facade;

    @BeforeEach
    void setUp() {
        facade = new HotelFacade(new RoomService(new RoomFactory()),
                new RateService(List.of(new HighSeasonPricingStrategy(), new LowSeasonPricingStrategy())),
                new AdditionalServiceService(new AdditionalServiceFactory()), new BillingService(), new AccessService());
    }

    private ReservationRequest request(int roomId) {
        return new ReservationRequest(roomId, START, END, Season.HIGH,
                new GuestRequest("Ana Pérez", "12345", "ana@example.com", "3001234567"));
    }

    @Test
    void completeFlowCoordinatesAllSubsystems() {
        assertThat(facade.availability(START, END, RoomType.DOUBLE, Season.HIGH)).hasSize(6);

        ReservationResponse created = facade.createReservation(request(7));
        assertThat(created.status()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(created.rate().total()).isEqualByComparingTo("675.00");
        assertThat(created.digitalKey()).isNull();
        assertThat(facade.availability(START, END, RoomType.DOUBLE, Season.HIGH)).hasSize(5);

        facade.addService(created.id(), ServiceType.BREAKFAST);
        ReservationResponse withSpa = facade.addService(created.id(), ServiceType.SPA);
        assertThat(withSpa.servicesTotal()).isEqualByComparingTo("95.00");
        assertThat(withSpa.total()).isEqualByComparingTo("770.00");

        assertThatThrownBy(() -> facade.performCheckOut(created.id()))
                .isInstanceOf(HotelException.class).extracting("code").isEqualTo("INVALID_STATE");

        ReservationResponse checkedIn = facade.performCheckIn(created.id());
        assertThat(checkedIn.status()).isEqualTo(ReservationStatus.CHECKED_IN);
        assertThat(checkedIn.digitalKey()).isNotNull();

        ReservationResponse checkedOut = facade.performCheckOut(created.id());
        assertThat(checkedOut.status()).isEqualTo(ReservationStatus.CHECKED_OUT);
        assertThat(checkedOut.digitalKey()).isNull();
        assertThat(checkedOut.invoice().total()).isEqualByComparingTo("770.00");
        assertThat(checkedOut.invoice().lines()).hasSize(4);
        assertThat(facade.availability(START, END, RoomType.DOUBLE, Season.HIGH)).hasSize(6);
        assertThat(facade.getReservation(created.id()).invoice()).isEqualTo(checkedOut.invoice());
    }

    @Test
    void reservationRegistryIsASingleton() {
        assertThat(ReservationRegistry.getInstance()).isSameAs(ReservationRegistry.getInstance());
        ReservationResponse created = facade.createReservation(request(13));
        assertThat(ReservationRegistry.getInstance().find(created.id())).isPresent();
    }
}
