-- Padroniza nomes em snake_case
ALTER TYPE eventstatus RENAME TO event_status;
ALTER TABLE events RENAME COLUMN startsat TO starts_at;
ALTER TABLE events RENAME COLUMN endsat TO ends_at;

-- Datas com fuso horário
ALTER TABLE events
    ALTER COLUMN starts_at TYPE TIMESTAMPTZ,
    ALTER COLUMN ends_at TYPE TIMESTAMPTZ,
    ALTER COLUMN created_at TYPE TIMESTAMPTZ;
ALTER TABLE users ALTER COLUMN created_at TYPE TIMESTAMPTZ;

-- Motivo do cancelamento e optimistic locking
ALTER TABLE events
    ADD COLUMN cancellation_reason VARCHAR(255),
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE events
    ADD CONSTRAINT chk_event_dates CHECK (ends_at > starts_at),
    ADD CONSTRAINT chk_event_capacity CHECK (capacity > 0);

CREATE INDEX idx_event_status ON events(status);
