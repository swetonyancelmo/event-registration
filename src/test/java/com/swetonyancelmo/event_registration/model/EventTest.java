package com.swetonyancelmo.event_registration.model;

import com.swetonyancelmo.event_registration.exception.BusinessRuleException;
import com.swetonyancelmo.event_registration.exception.InvalidStateTransitionException;
import com.swetonyancelmo.event_registration.model.enums.EventStatus;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventTest {

    private static final OffsetDateTime START = OffsetDateTime.now().plusDays(10);

    private Event draft() {
        return Event.draft("Title", "Description", START, START.plusHours(2), "Online", 50, new User());
    }

    @Test
    void newEventStartsAsDraft() {
        assertThat(draft().getStatus()).isEqualTo(EventStatus.DRAFT);
    }

    @Test
    void rejectsEndBeforeStart() {
        assertThatThrownBy(() -> Event.draft("T", "D", START, START.minusHours(1), "L", 10, new User()))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rejectsNonPositiveCapacity() {
        assertThatThrownBy(() -> Event.draft("T", "D", START, START.plusHours(1), "L", 0, new User()))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void publishesDraft() {
        Event event = draft();
        event.publish();
        assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
    }

    @Test
    void cannotPublishTwice() {
        Event event = draft();
        event.publish();
        assertThatThrownBy(event::publish).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void cannotFinishDraft() {
        assertThatThrownBy(draft()::finish).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void finishesPublishedEvent() {
        Event event = draft();
        event.publish();
        event.finish();
        assertThat(event.getStatus()).isEqualTo(EventStatus.FINISHED);
    }

    @Test
    void cancelStoresReason() {
        Event event = draft();
        event.publish();
        event.cancel("Speaker unavailable");
        assertThat(event.getStatus()).isEqualTo(EventStatus.CANCELLED);
        assertThat(event.getCancellationReason()).isEqualTo("Speaker unavailable");
    }

    @Test
    void cannotCancelFinishedOrCancelledEvent() {
        Event finished = draft();
        finished.publish();
        finished.finish();
        assertThatThrownBy(() -> finished.cancel("x")).isInstanceOf(InvalidStateTransitionException.class);

        Event cancelled = draft();
        cancelled.cancel("x");
        assertThatThrownBy(() -> cancelled.cancel("y")).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void cannotPublishCancelledEvent() {
        Event event = draft();
        event.cancel("x");
        assertThatThrownBy(event::publish).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void onlyDraftCanBeEdited() {
        Event event = draft();
        event.publish();
        assertThatThrownBy(() -> event.update("T", "D", START, START.plusHours(1), "L", 10))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

}
