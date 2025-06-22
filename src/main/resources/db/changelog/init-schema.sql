CREATE TABLE users
(
    id         BIGSERIAL PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE units
(
    id                 BIGSERIAL PRIMARY KEY,
    number_of_rooms    INTEGER        NOT NULL,
    accommodation_type VARCHAR(50)    NOT NULL CHECK (accommodation_type IN ('HOME', 'FLAT', 'APARTMENTS')),
    floor_number       INTEGER        NOT NULL,
    base_cost          DECIMAL(10, 2) NOT NULL,
    final_cost         DECIMAL(10, 2) NOT NULL,
    description        TEXT           NOT NULL,
    owner_id           BIGINT         NOT NULL REFERENCES users (id),
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE bookings
(
    id         BIGSERIAL PRIMARY KEY,
    unit_id    BIGINT         NOT NULL REFERENCES units (id),
    user_id    BIGINT         NOT NULL REFERENCES users (id),
    start_date DATE           NOT NULL,
    end_date   DATE           NOT NULL,
    total_cost DECIMAL(10, 2) NOT NULL,
    status     VARCHAR(20)    NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'EXPIRED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP
);

CREATE TABLE payments
(
    id             BIGSERIAL PRIMARY KEY,
    booking_id     BIGINT         NOT NULL REFERENCES bookings (id),
    amount         DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(100),
    transaction_id VARCHAR(255),
    processed_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE events
(
    id          BIGSERIAL PRIMARY KEY,
    event_type  VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50)  NOT NULL,
    entity_id   BIGINT       NOT NULL,
    event_data  TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);