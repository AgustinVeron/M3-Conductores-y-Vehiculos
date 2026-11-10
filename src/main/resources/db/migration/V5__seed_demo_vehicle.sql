-- Migration V5: Seed demo driver and vehicle for development and testing

INSERT INTO drivers (id, identity_subject, display_name, qualification_status, version, created_at, updated_at)
VALUES (
    'd0000000-0000-0000-0000-000000000001',
    'demo-driver-001',
    'Conductor Demo',
    'APPROVED',
    1,
    NOW(),
    NOW()
) ON CONFLICT (id) DO NOTHING;

INSERT INTO vehicles (id, driver_id, vehicle_type, plate, make, model, model_year, qualification_status, driver_version, created_at)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'd0000000-0000-0000-0000-000000000001',
    'CAR',
    'AB123CD',
    'Toyota',
    'Corolla',
    2022,
    'APPROVED',
    1,
    NOW()
) ON CONFLICT (id) DO NOTHING;
