-- =============================================================
-- Migration V2: Availability intents, audit log, outbox,
--               and idempotency records (HU-10)
-- =============================================================

-- -------------------------------------------------------------
-- availability_intents
-- One row per driver. The UNIQUE constraint on driver_id enforces
-- the "one intent per driver" business rule at the DB level.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS availability_intents (
    id              UUID        PRIMARY KEY,
    driver_id       UUID        NOT NULL,
    vehicle_id      UUID        NOT NULL,

    -- Requested state (what the driver wants)
    connected       BOOLEAN     NOT NULL DEFAULT FALSE,
    available       BOOLEAN     NOT NULL DEFAULT FALSE,

    -- Synchronisation state with M4
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
        -- PENDING | RETRYING | APPLIED | FAILED

    -- Optimistic versioning for M4 de-duplication
    desired_version BIGINT      NOT NULL DEFAULT 1,
    applied_version BIGINT,

    -- Timestamps
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    synced_at       TIMESTAMPTZ,

    -- Constraints
    CONSTRAINT availability_intents_driver_id_uq UNIQUE (driver_id),
    CONSTRAINT availability_intents_connected_available_ck
        CHECK (connected = TRUE OR available = FALSE),
        -- connected=false → available must be false
    CONSTRAINT availability_intents_status_ck
        CHECK (status IN ('PENDING', 'RETRYING', 'APPLIED', 'FAILED'))
);

CREATE INDEX IF NOT EXISTS idx_avail_intents_driver_id
    ON availability_intents (driver_id);

CREATE INDEX IF NOT EXISTS idx_avail_intents_status
    ON availability_intents (status)
    WHERE status IN ('PENDING', 'RETRYING');

-- -------------------------------------------------------------
-- audit_log
-- Append-only log of relevant domain events.
-- -------------------------------------------------------------
-- audit_log already exists in V1; keep its shared schema for document and availability audits.
CREATE INDEX IF NOT EXISTS idx_audit_log_entity ON audit_log (entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_created_at ON audit_log (created_at);

-- -------------------------------------------------------------
-- outbox
-- Transactional outbox for HTTP messages to M4 (E1).
-- Expanded to events in E2.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS outbox (
    id              UUID        PRIMARY KEY,
    destination     VARCHAR(200) NOT NULL,   -- e.g. M4 availability-intent endpoint
    message_type    VARCHAR(100) NOT NULL,
    aggregate_type  VARCHAR(80)  NOT NULL,
    aggregate_id    UUID         NOT NULL,
    aggregate_version BIGINT     NOT NULL,
    payload         TEXT         NOT NULL,   -- JSON
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
        -- PENDING | PROCESSING | SENT | FAILED
    attempts        INTEGER      NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    processed_at    TIMESTAMPTZ,

    CONSTRAINT outbox_status_ck
        CHECK (status IN ('PENDING', 'PROCESSING', 'SENT', 'FAILED'))
);

CREATE INDEX IF NOT EXISTS idx_outbox_status_next
    ON outbox (status, next_attempt_at)
    WHERE status IN ('PENDING', 'RETRYING');

-- -------------------------------------------------------------
-- idempotency_records
-- Stores the result of mutable operations keyed by
-- (principal, method, normalized_path, idempotency_key).
-- Retention: 7 days (enforced by a scheduled cleanup job).
-- -------------------------------------------------------------
-- idempotency_records is also shared with document registration (V1).
CREATE INDEX IF NOT EXISTS idx_idempotency_created_at ON idempotency_records (created_at);
