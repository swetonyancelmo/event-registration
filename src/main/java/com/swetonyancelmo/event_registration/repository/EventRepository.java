package com.swetonyancelmo.event_registration.repository;

import com.swetonyancelmo.event_registration.model.Event;
import com.swetonyancelmo.event_registration.model.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

    @EntityGraph(attributePaths = "organizer")
    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "organizer")
    Page<Event> findByStatusAndOrganizerId(EventStatus status, UUID organizerId, Pageable pageable);

}
