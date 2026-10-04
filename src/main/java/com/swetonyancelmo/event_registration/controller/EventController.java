package com.swetonyancelmo.event_registration.controller;

import com.swetonyancelmo.event_registration.dtos.CreateEventDTO;
import com.swetonyancelmo.event_registration.dtos.EventResponseDTO;
import com.swetonyancelmo.event_registration.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping("/draft")
    public ResponseEntity<EventResponseDTO> eventDraft(@RequestBody @Valid CreateEventDTO request) {
        EventResponseDTO response = eventService.createEventDraft(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
