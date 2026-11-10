-- V1__initial_schema.sql: Esquema inicial E1 para M3
CREATE TABLE IF NOT EXISTS drivers (
    id UUID PRIMARY KEY,
    identity_subject VARCHAR(128) UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    qualification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    qualification_reason VARCHAR(500),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    dni VARCHAR(32),
    birth_date DATE,
    address VARCHAR(255),
    phone VARCHAR(50),
    email VARCHAR(150),
    version BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS vehicles (
    id UUID PRIMARY KEY,
    driver_id UUID NOT NULL,
    vehicle_type VARCHAR(50),
    service_types VARCHAR(50),
    country_code VARCHAR(2) DEFAULT 'AR',
    plate VARCHAR(20) NOT NULL,
    make VARCHAR(60) NOT NULL,
    model VARCHAR(60) NOT NULL,
    model_year INTEGER,
    qualification_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    driver_version BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vehicles_driver FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_vehicles_driver_id ON vehicles(driver_id);

CREATE TABLE IF NOT EXISTS documents (
    id UUID PRIMARY KEY,
    driver_id UUID NOT NULL,
    vehicle_id UUID,
    document_type VARCHAR(50) NOT NULL,
    document_number VARCHAR(80) NOT NULL,
    issuing_entity VARCHAR(100),
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL,
    valid_until TIMESTAMP WITH TIME ZONE NOT NULL,
    storage_reference VARCHAR(200),
    revision INTEGER NOT NULL DEFAULT 1,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    is_current BOOLEAN NOT NULL DEFAULT TRUE,
    service_types VARCHAR(50),
    driver_version BIGINT NOT NULL DEFAULT 1,
    rejection_reason VARCHAR(500),
    reviewed_at TIMESTAMP WITH TIME ZONE,
    reviewed_by VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_documents_driver FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE CASCADE,
    CONSTRAINT fk_documents_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_documents_driver_id ON documents(driver_id);
CREATE INDEX IF NOT EXISTS idx_documents_vehicle_type_current ON documents(vehicle_id, document_type, is_current);

CREATE TABLE IF NOT EXISTS audit_log (
    id UUID PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    driver_id UUID,
    action VARCHAR(50) NOT NULL,
    actor VARCHAR(100) NOT NULL,
    previous_status VARCHAR(50),
    new_status VARCHAR(50),
    reason VARCHAR(500),
    correlation_id VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_log_driver_id ON audit_log(driver_id);

CREATE TABLE IF NOT EXISTS idempotency_records (
    id UUID PRIMARY KEY,
    idempotency_key VARCHAR(128) NOT NULL,
    principal VARCHAR(100) NOT NULL,
    http_method VARCHAR(10) NOT NULL,
    request_path VARCHAR(255) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    response_status INTEGER NOT NULL,
    response_body TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_idempotency UNIQUE (idempotency_key, http_method, request_path)
);
