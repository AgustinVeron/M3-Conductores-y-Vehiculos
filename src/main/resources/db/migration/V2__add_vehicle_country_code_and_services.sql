ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS country_code VARCHAR(2) NOT NULL DEFAULT 'AR';

CREATE TABLE IF NOT EXISTS vehicle_services (
    vehicle_id UUID NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
    service_type VARCHAR(50) NOT NULL,
    PRIMARY KEY (vehicle_id, service_type)
);

CREATE INDEX IF NOT EXISTS idx_vehicle_services_vehicle_id ON vehicle_services(vehicle_id);