package com.hotel.service;

import com.hotel.exception.HotelException;
import com.hotel.factory.RoomFactory;
import com.hotel.model.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.IntStream;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class RoomService {
    private final List<Room> rooms;
    private final Map<UUID, Booking> bookings = new HashMap<>();

    private record Booking(int roomId, LocalDate start, LocalDate end) {}

    public RoomService(RoomFactory factory) {
        rooms = IntStream.rangeClosed(1, 15).mapToObj(id -> factory.create(id,
                id <= 6 ? RoomType.SINGLE : id <= 12 ? RoomType.DOUBLE : RoomType.SUITE)).toList();
    }

    public Room find(int id) {
        return rooms.stream().filter(room -> room.id() == id).findFirst().orElseThrow(() ->
                new HotelException(HttpStatus.NOT_FOUND, "ROOM_NOT_FOUND", "La habitación no existe."));
    }

    public synchronized List<Room> available(LocalDate start, LocalDate end, RoomType type) {
        StayRules.validate(start, end);
        return rooms.stream().filter(r -> type == null || r.type() == type)
                .filter(r -> isAvailable(r.id(), start, end)).toList();
    }

    public synchronized void reserve(UUID reservationId, int roomId, LocalDate start, LocalDate end) {
        StayRules.validate(start, end);
        find(roomId);
        if (!isAvailable(roomId, start, end)) {
            throw new HotelException(HttpStatus.CONFLICT, "ROOM_UNAVAILABLE", "La habitación no está disponible para las fechas seleccionadas.");
        }
        bookings.put(reservationId, new Booking(roomId, start, end));
    }

    public synchronized void release(UUID reservationId) { bookings.remove(reservationId); }

    private boolean isAvailable(int roomId, LocalDate start, LocalDate end) {
        return bookings.values().stream().noneMatch(b -> b.roomId() == roomId && start.isBefore(b.end()) && end.isAfter(b.start()));
    }
}
