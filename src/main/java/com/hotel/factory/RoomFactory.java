package com.hotel.factory;

import com.hotel.model.Room;
import com.hotel.model.RoomType;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class RoomFactory {
    public Room create(int id, RoomType type) {
        String price = switch (type) { case SINGLE -> "100.00"; case DOUBLE -> "150.00"; case SUITE -> "250.00"; };
        return new Room(id, String.valueOf(100 + id), type, new BigDecimal(price));
    }
}
