DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS units;
DROP TABLE IF EXISTS users;

CREATE TABLE users
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE units
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    number_of_rooms    INTEGER        NOT NULL,
    accommodation_type VARCHAR(50)    NOT NULL CHECK (accommodation_type IN ('HOME', 'FLAT', 'APARTMENTS')),
    floor_number       INTEGER        NOT NULL,
    base_cost          DECIMAL(10, 2) NOT NULL,
    final_cost         DECIMAL(10, 2),
    description        TEXT           NOT NULL,
    owner_id           BIGINT         NOT NULL,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users (id)
);

CREATE TABLE bookings
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    unit_id    BIGINT         NOT NULL,
    user_id    BIGINT         NOT NULL,
    start_date DATE           NOT NULL,
    end_date   DATE           NOT NULL,
    total_cost DECIMAL(10, 2),
    status     VARCHAR(20)    NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'EXPIRED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    FOREIGN KEY (unit_id) REFERENCES units (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);