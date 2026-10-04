package com.swetonyancelmo.event_registration.service;

import com.swetonyancelmo.event_registration.dtos.CreateEventDTO;
import com.swetonyancelmo.event_registration.dtos.EventResponseDTO;
import com.swetonyancelmo.event_registration.exception.BusinessRuleException;
import com.swetonyancelmo.event_registration.exception.ResourceNotFoundException;
import com.swetonyancelmo.event_registration.model.Event;
import com.swetonyancelmo.event_registration.model.User;
import com.swetonyancelmo.event_registration.model.enums.EventStatus;
import com.swetonyancelmo.event_registration.model.enums.UserRole;
import com.swetonyancelmo.event_registration.repository.EventRepository;
import com.swetonyancelmo.event_registration.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Transactional
    public EventResponseDTO createEventDraft(CreateEventDTO dto) {
        User organizer = userRepository.findById(dto.organizer_id()).orElseThrow(() -> new ResourceNotFoundException("Organizer not found"));

        if (organizer.getRole() != UserRole.ADMIN) {
            throw new BusinessRuleException("Only admins can create events");
        }

        Event event = new Event();
        event.setTitle(dto.title());
        event.setDescription(dto.description());
        event.setStartsAt(dto.startsAt());
        event.setEndsAt(dto.endsAt());
        event.setLocation(dto.location());
        event.setCapacity(dto.capacity());
        event.setStatus(EventStatus.DRAFT);
        event.setOrganizer(organizer);

        Event eventDraft = eventRepository.save(event);

        return new EventResponseDTO(
                eventDraft.getId(), eventDraft.getTitle(),
                eventDraft.getDescription(), eventDraft.getStartsAt(),
                eventDraft.getEndsAt(), eventDraft.getLocation(),
                eventDraft.getCapacity(), eventDraft.getStatus(),
                eventDraft.getOrganizer().getName()
        );
    }

}
