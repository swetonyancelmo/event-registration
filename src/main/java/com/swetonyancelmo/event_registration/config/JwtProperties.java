package com.swetonyancelmo.event_registration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, Duration expiration) {
}
