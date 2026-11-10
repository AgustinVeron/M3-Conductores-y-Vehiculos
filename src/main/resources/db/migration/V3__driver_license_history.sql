CREATE TABLE document_service_types (
    document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    service_type VARCHAR(10) NOT NULL,
    PRIMARY KEY (document_id, service_type),
    CONSTRAINT ck_document_service_type CHECK (service_type IN ('AUTO', 'MOTO'))
);

CREATE UNIQUE INDEX uq_current_driver_license
    ON documents (driver_id, document_type)
    WHERE vehicle_id IS NULL AND is_current;

CREATE UNIQUE INDEX uq_driver_license_revision
    ON documents (driver_id, document_type, revision)
    WHERE vehicle_id IS NULL;

ALTER TABLE documents ADD CONSTRAINT ck_license_owner
    CHECK ((document_type = 'LICENSE' AND vehicle_id IS NULL)
        OR (document_type <> 'LICENSE' AND vehicle_id IS NOT NULL));

ALTER TABLE documents ADD CONSTRAINT ck_document_period CHECK (issued_at < valid_until);
