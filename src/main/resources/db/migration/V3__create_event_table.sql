CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TYPE eventStatus AS ENUM ('DRAFT', 'PUBLISHED', 'CANCELLED', 'FINISHED');

CREATE TABLE events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    startsAt TIMESTAMP NOT NULL,
    endsAt TIMESTAMP NOT NULL,
    location VARCHAR(255) NOT NULL,
    capacity INTEGER NOT NULL,
    status eventStatus NOT NULL,
    organizer_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_event_organizer
            FOREIGN KEY (organizer_id) REFERENCES users(id)
);

CREATE INDEX idx_event_organizer ON events(organizer_id);