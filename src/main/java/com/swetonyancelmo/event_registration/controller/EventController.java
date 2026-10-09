package com.swetonyancelmo.event_registration.controller;

import com.swetonyancelmo.event_registration.dtos.*;
import com.swetonyancelmo.event_registration.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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

    @PostMapping("/publish")
    public ResponseEntity<EventResponseDTO> publishEvent(@RequestBody @Valid PublishEventRequestDTO request) {
        EventResponseDTO response = eventService.publishEvent(request.eventId(), request.organizerId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/cancel")
    public ResponseEntity<EventCancelledResponseDTO> cancelEvent(@RequestBody @Valid EventCancelledRequestDTO request) {
        EventCancelledResponseDTO response = eventService.cancelEvent(request.eventId(), request.reason(), request.organizerId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/finish")
    public  ResponseEntity<String> finishEvent(@RequestBody @Valid FinishEventRequestDTO request) {
        String eventFinished = eventService.finishEvent(request.eventId(), request.organizerId());
        return ResponseEntity.status(HttpStatus.OK).body(eventFinished);
    }

}
