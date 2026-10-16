package com.hotel.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AccessService {
    private final Map<UUID, UUID> keys = new HashMap<>();
    public synchronized UUID generateKey(UUID reservationId) {
        return keys.computeIfAbsent(reservationId, ignored -> UUID.randomUUID());
    }
    public synchronized void revokeKey(UUID reservationId) { keys.remove(reservationId); }
}
