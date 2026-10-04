package com.swetonyancelmo.event_registration.repository;

import com.swetonyancelmo.event_registration.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
}
