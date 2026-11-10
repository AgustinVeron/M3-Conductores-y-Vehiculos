CREATE TABLE qualification_decisions (
 id UUID PRIMARY KEY, driver_id UUID NOT NULL REFERENCES drivers(id), vehicle_id UUID REFERENCES vehicles(id),
 target_id UUID NOT NULL, actor VARCHAR(128) NOT NULL, request_key VARCHAR(128) NOT NULL,
 fingerprint VARCHAR(600) NOT NULL, body TEXT NOT NULL, created_at TIMESTAMPTZ NOT NULL,
 UNIQUE(target_id,actor,request_key)
);
CREATE INDEX idx_qualification_history ON qualification_decisions(driver_id,target_id,created_at,id);
CREATE TABLE customer_ratings (
 id UUID PRIMARY KEY, driver_id UUID NOT NULL REFERENCES drivers(id), trip_id VARCHAR(128) NOT NULL,
 client_id VARCHAR(128) NOT NULL, score INTEGER NOT NULL CHECK(score BETWEEN 1 AND 5), comment VARCHAR(500),
 request_key VARCHAR(128) NOT NULL, fingerprint VARCHAR(700) NOT NULL, created_at TIMESTAMPTZ NOT NULL,
 UNIQUE(driver_id,trip_id), UNIQUE(driver_id,request_key)
);
CREATE INDEX idx_rating_history ON customer_ratings(driver_id,created_at,id);
