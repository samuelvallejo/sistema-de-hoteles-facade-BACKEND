package com.hotel.factory;

import com.hotel.model.AdditionalService;
import com.hotel.model.ServiceType;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class AdditionalServiceFactory {
    public AdditionalService create(ServiceType type, long nights) {
        String description = switch (type) { case SPA -> "Spa"; case BREAKFAST -> "Desayuno por día"; case TRANSFER -> "Traslado"; };
        BigDecimal price = new BigDecimal(switch (type) { case SPA -> "50.00"; case BREAKFAST -> "15.00"; case TRANSFER -> "30.00"; });
        long quantity = type == ServiceType.BREAKFAST ? nights : 1;
        return new AdditionalService(type, description, quantity, price, price.multiply(BigDecimal.valueOf(quantity)));
    }
}
