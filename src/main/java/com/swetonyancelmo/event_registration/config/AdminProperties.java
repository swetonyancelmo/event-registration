package com.swetonyancelmo.event_registration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.admin")
public record AdminProperties(String name, String email, String password) {
}
