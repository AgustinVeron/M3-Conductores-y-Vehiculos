-- Persist the extra fields supplied by the integrated HU-03 frontend.
ALTER TABLE vehicles ADD COLUMN color VARCHAR(30);
ALTER TABLE vehicles ADD COLUMN capacity INTEGER;
ALTER TABLE vehicles ADD COLUMN service_categories VARCHAR(100);
ALTER TABLE vehicles ADD CONSTRAINT ck_vehicle_capacity CHECK (capacity BETWEEN 1 AND 6);
