package com.swetonyancelmo.event_registration.service;

import com.swetonyancelmo.event_registration.dtos.CreateEventDTO;
import com.swetonyancelmo.event_registration.dtos.EventResponseDTO;
import com.swetonyancelmo.event_registration.exception.InvalidRequestParameterException;
import com.swetonyancelmo.event_registration.exception.ResourceNotFoundException;
import com.swetonyancelmo.event_registration.model.Event;
import com.swetonyancelmo.event_registration.model.User;
import com.swetonyancelmo.event_registration.model.enums.EventStatus;
import com.swetonyancelmo.event_registration.repository.EventRepository;
import com.swetonyancelmo.event_registration.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {

    private static final List<String> SORTABLE_FIELDS = List.of("startsAt", "endsAt", "title", "capacity", "createdAt");

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Transactional
    public EventResponseDTO create(CreateEventDTO dto, UUID organizerId) {
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found"));

        Event event = Event.draft(dto.title(), dto.description(), dto.startsAt(), dto.endsAt(),
                dto.location(), dto.capacity(), organizer);

        return EventResponseDTO.from(eventRepository.save(event));
    }

    @Transactional(readOnly = true)
    public Page<EventResponseDTO> list(EventStatus status, UUID viewerId, Pageable pageable) {
        validateSort(pageable.getSort());

        if (status == EventStatus.DRAFT) {
            if (viewerId == null) {
                return Page.empty(pageable);
            }
            return eventRepository.findByStatusAndOrganizerId(status, viewerId, pageable).map(EventResponseDTO::from);
        }
        return eventRepository.findByStatus(status, pageable).map(EventResponseDTO::from);
    }

    @Transactional(readOnly = true)
    public EventResponseDTO findById(UUID eventId, UUID viewerId) {
        Event event = findEvent(eventId);
        if (event.getStatus() == EventStatus.DRAFT && (viewerId == null || !event.isOrganizedBy(viewerId))) {
            throw new ResourceNotFoundException("Event not found");
        }
        return EventResponseDTO.from(event);
    }

    @Transactional
    public EventResponseDTO update(UUID eventId, CreateEventDTO dto, UUID userId) {
        Event event = findOwnedEvent(eventId, userId);
        event.update(dto.title(), dto.description(), dto.startsAt(), dto.endsAt(), dto.location(), dto.capacity());
        return EventResponseDTO.from(event);
    }

    @Transactional
    public EventResponseDTO publish(UUID eventId, UUID userId) {
        Event event = findOwnedEvent(eventId, userId);
        event.publish();
        return EventResponseDTO.from(event);
    }

    @Transactional
    public EventResponseDTO cancel(UUID eventId, String reason, UUID userId) {
        Event event = findOwnedEvent(eventId, userId);
        event.cancel(reason);
        return EventResponseDTO.from(event);
    }

    @Transactional
    public EventResponseDTO finish(UUID eventId, UUID userId) {
        Event event = findOwnedEvent(eventId, userId);
        event.finish();
        return EventResponseDTO.from(event);
    }

    private static void validateSort(Sort sort) {
        sort.stream()
                .filter(order -> !SORTABLE_FIELDS.contains(order.getProperty()))
                .findFirst()
                .ifPresent(order -> {
                    throw new InvalidRequestParameterException("Invalid sort property '" + order.getProperty()
                            + "'. Allowed values: " + String.join(", ", SORTABLE_FIELDS));
                });
    }

    private Event findOwnedEvent(UUID eventId, UUID userId) {
        Event event = findEvent(eventId);
        if (!event.isOrganizedBy(userId)) {
            throw new AccessDeniedException("Only the event organizer can modify this event");
        }
        return event;
    }

    private Event findEvent(UUID eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> new ResourceNotFoundException("Event not found"));
    }

}
