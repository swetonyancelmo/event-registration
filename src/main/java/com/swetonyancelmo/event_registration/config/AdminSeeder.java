package com.swetonyancelmo.event_registration.config;

import com.swetonyancelmo.event_registration.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Cria o primeiro administrador a partir de app.admin.* (variáveis ADMIN_*), caso ainda não exista.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

    private final AdminProperties properties;
    private final UserService userService;

    @Override
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(properties.email()) || !StringUtils.hasText(properties.password())) {
            return;
        }
        if (userService.createAdminIfAbsent(properties.name(), properties.email(), properties.password())) {
            log.info("Initial admin user created: {}", properties.email());
        }
    }

}
