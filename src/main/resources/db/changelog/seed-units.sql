INSERT INTO users (email, name)
VALUES ('system@booking.com', 'System User');

INSERT INTO units (number_of_rooms, accommodation_type, floor_number, base_cost, final_cost, description, owner_id)
VALUES (2, 'FLAT', 3, 150.00, 172.50, 'Cozy 2-room flat on the 3rd floor with modern amenities', 1),
       (1, 'APARTMENTS', 5, 200.00, 230.00, 'Luxury studio apartment with city view', 1),
       (3, 'HOME', 1, 300.00, 345.00, 'Spacious 3-bedroom house with garden', 1),
       (2, 'FLAT', 7, 175.00, 201.25, 'Modern 2-room flat with balcony', 1),
       (4, 'HOME', 2, 400.00, 460.00, 'Large family house with 4 bedrooms', 1),
       (1, 'APARTMENTS', 10, 120.00, 138.00, 'Compact apartment perfect for business trips', 1),
       (2, 'FLAT', 4, 160.00, 184.00, 'Bright 2-room flat with good location', 1),
       (3, 'APARTMENTS', 8, 250.00, 287.50, 'Premium 3-room apartment with amenities', 1),
       (5, 'HOME', 1, 500.00, 575.00, 'Luxury villa with 5 bedrooms and pool', 1),
       (1, 'FLAT', 6, 130.00, 149.50, 'Simple 1-room flat for short stays', 1);

INSERT INTO events (event_type, entity_type, entity_id, event_data)
VALUES ('UNIT_CREATED', 'Unit', 1, 'Initial unit: Cozy 2-room flat on the 3rd floor'),
       ('UNIT_CREATED', 'Unit', 2, 'Initial unit: Luxury studio apartment with city view'),
       ('UNIT_CREATED', 'Unit', 3, 'Initial unit: Spacious 3-bedroom house with garden'),
       ('UNIT_CREATED', 'Unit', 4, 'Initial unit: Modern 2-room flat with balcony'),
       ('UNIT_CREATED', 'Unit', 5, 'Initial unit: Large family house with 4 bedrooms'),
       ('UNIT_CREATED', 'Unit', 6, 'Initial unit: Compact apartment perfect for business trips'),
       ('UNIT_CREATED', 'Unit', 7, 'Initial unit: Bright 2-room flat with good location'),
       ('UNIT_CREATED', 'Unit', 8, 'Initial unit: Premium 3-room apartment with amenities'),
       ('UNIT_CREATED', 'Unit', 9, 'Initial unit: Luxury villa with 5 bedrooms and pool'),
       ('UNIT_CREATED', 'Unit', 10, 'Initial unit: Simple 1-room flat for short stays');