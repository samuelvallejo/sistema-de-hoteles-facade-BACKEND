package com.hotel.service;

import com.hotel.factory.AdditionalServiceFactory;
import com.hotel.model.AdditionalService;
import com.hotel.model.ServiceType;
import org.springframework.stereotype.Service;

@Service
public class AdditionalServiceService {
    private final AdditionalServiceFactory factory;
    public AdditionalServiceService(AdditionalServiceFactory factory) { this.factory = factory; }
    public AdditionalService addService(ServiceType type, long nights) { return factory.create(type, nights); }
}
