package com.swetonyancelmo.event_registration.model;

import com.swetonyancelmo.event_registration.exception.BusinessRuleException;
import com.swetonyancelmo.event_registration.exception.InvalidStateTransitionException;
import com.swetonyancelmo.event_registration.model.enums.EventStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private OffsetDateTime startsAt;

    @Column(nullable = false)
    private OffsetDateTime endsAt;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private EventStatus status;

    private String cancellationReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public static Event draft(String title, String description, OffsetDateTime startsAt, OffsetDateTime endsAt,
                              String location, Integer capacity, User organizer) {
        Event event = new Event();
        event.organizer = organizer;
        event.status = EventStatus.DRAFT;
        event.applyDetails(title, description, startsAt, endsAt, location, capacity);
        return event;
    }

    public void update(String title, String description, OffsetDateTime startsAt, OffsetDateTime endsAt,
                       String location, Integer capacity) {
        if (status != EventStatus.DRAFT) {
            throw new InvalidStateTransitionException("Only DRAFT events can be edited");
        }
        applyDetails(title, description, startsAt, endsAt, location, capacity);
    }

    public void publish() {
        if (status != EventStatus.DRAFT) {
            throw new InvalidStateTransitionException("Only DRAFT events can be published");
        }
        this.status = EventStatus.PUBLISHED;
    }

    public void cancel(String reason) {
        if (status == EventStatus.FINISHED || status == EventStatus.CANCELLED) {
            throw new InvalidStateTransitionException("Event cannot be cancelled from status " + status);
        }
        this.status = EventStatus.CANCELLED;
        this.cancellationReason = reason;
    }

    public void finish() {
        if (status != EventStatus.PUBLISHED) {
            throw new InvalidStateTransitionException("Only PUBLISHED events can be finished");
        }
        this.status = EventStatus.FINISHED;
    }

    public boolean isOrganizedBy(UUID userId) {
        return organizer.getId().equals(userId);
    }

    private void applyDetails(String title, String description, OffsetDateTime startsAt, OffsetDateTime endsAt,
                              String location, Integer capacity) {
        if (!endsAt.isAfter(startsAt)) {
            throw new BusinessRuleException("endsAt must be after startsAt");
        }
        if (capacity <= 0) {
            throw new BusinessRuleException("capacity must be positive");
        }
        this.title = title;
        this.description = description;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.location = location;
        this.capacity = capacity;
    }

}
