package com.hotel.config;

import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hotel.frontend")
public record FrontendProperties(URI url) {}
