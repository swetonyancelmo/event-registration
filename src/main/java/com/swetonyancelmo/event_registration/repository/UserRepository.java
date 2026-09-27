package com.swetonyancelmo.event_registration.repository;

import com.swetonyancelmo.event_registration.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Boolean existsByEmail(String email);
}
