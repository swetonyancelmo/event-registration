package com.swetonyancelmo.event_registration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/v1/users", "/api/v1/users/admin", "/api/v1/events", "/api/v1/events/draft",
                                "/api/v1/events/publish", "/api/v1/events/cancel", "/api/v1/events/finish").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }

}
