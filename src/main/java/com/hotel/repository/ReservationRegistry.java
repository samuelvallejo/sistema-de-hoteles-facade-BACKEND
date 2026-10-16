package com.hotel.repository;

import com.hotel.model.Reservation;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton: single in-memory registry of reservations shared by the whole application.
 * Only HotelFacade uses it; controllers and subsystems never access it directly.
 */
public final class ReservationRegistry {
    private final Map<UUID, Reservation> reservations = new ConcurrentHashMap<>();

    private ReservationRegistry() {}

    private static final class Holder {
        private static final ReservationRegistry INSTANCE = new ReservationRegistry();
    }

    public static ReservationRegistry getInstance() { return Holder.INSTANCE; }

    public void save(Reservation reservation) { reservations.put(reservation.id(), reservation); }

    public Optional<Reservation> find(UUID id) { return Optional.ofNullable(reservations.get(id)); }

    public int size() { return reservations.size(); }
}
