-- Test data for demo purposes
-- This file runs automatically on startup when spring.sql.init.mode=always
-- Uses INSERT with ON CONFLICT DO NOTHING for PostgreSQL compatibility

-- Insert ride locations first
INSERT INTO ride_location (ride_location_id, latitude, longitude, display_address) VALUES
('11111111-1111-1111-1111-111111111111', 54.6872, 25.2797, 'Vilnius City Center')
ON CONFLICT (ride_location_id) DO NOTHING;

INSERT INTO ride_location (ride_location_id, latitude, longitude, display_address) VALUES
('11111111-1111-1111-1111-111111111112', 54.8969, 23.8927, 'Kaunas City Center')
ON CONFLICT (ride_location_id) DO NOTHING;

INSERT INTO ride_location (ride_location_id, latitude, longitude, display_address) VALUES
('22222222-2222-2222-2222-222222222221', 54.6372, 25.2297, 'Vilnius Airport')
ON CONFLICT (ride_location_id) DO NOTHING;

INSERT INTO ride_location (ride_location_id, latitude, longitude, display_address) VALUES
('22222222-2222-2222-2222-222222222222', 54.8469, 23.8427, 'Kaunas Airport')
ON CONFLICT (ride_location_id) DO NOTHING;

INSERT INTO ride_location (ride_location_id, latitude, longitude, display_address) VALUES
('33333333-3333-3333-3333-333333333331', 54.7072, 25.2997, 'Vilnius Old Town')
ON CONFLICT (ride_location_id) DO NOTHING;

INSERT INTO ride_location (ride_location_id, latitude, longitude, display_address) VALUES
('33333333-3333-3333-3333-333333333332', 54.9169, 23.9127, 'Kaunas Old Town')
ON CONFLICT (ride_location_id) DO NOTHING;

-- Insert rides
INSERT INTO ride (ride_id, driver_id, vehicle_id, available_seats, ride_start_date, seat_price_amount, seat_price_currency, status, start_location_id, end_location_id) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440001', 3, '2026-05-15T08:00:00', 15.00, 'EUR', 'PENDING', '11111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111112')
ON CONFLICT (ride_id) DO NOTHING;

INSERT INTO ride (ride_id, driver_id, vehicle_id, available_seats, ride_start_date, seat_price_amount, seat_price_currency, status, start_location_id, end_location_id) VALUES
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440001', 2, '2026-05-16T14:30:00', 20.00, 'EUR', 'PENDING', '22222222-2222-2222-2222-222222222221', '22222222-2222-2222-2222-222222222222')
ON CONFLICT (ride_id) DO NOTHING;

INSERT INTO ride (ride_id, driver_id, vehicle_id, available_seats, ride_start_date, seat_price_amount, seat_price_currency, status, start_location_id, end_location_id) VALUES
('cccccccc-cccc-cccc-cccc-cccccccccccc', '660e8400-e29b-41d4-a716-446655440002', '660e8400-e29b-41d4-a716-446655440003', 4, '2026-05-10T09:00:00', 12.50, 'EUR', 'COMPLETED', '33333333-3333-3333-3333-333333333331', '33333333-3333-3333-3333-333333333332')
ON CONFLICT (ride_id) DO NOTHING;

INSERT INTO ride (ride_id, driver_id, vehicle_id, available_seats, ride_start_date, seat_price_amount, seat_price_currency, status, start_location_id, end_location_id) VALUES
('dddddddd-dddd-dddd-dddd-dddddddddddd', '770e8400-e29b-41d4-a716-446655440004', '770e8400-e29b-41d4-a716-446655440005', 1, '2026-05-20T16:00:00', 25.00, 'EUR', 'PENDING', '11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222')
ON CONFLICT (ride_id) DO NOTHING;

INSERT INTO ride (ride_id, driver_id, vehicle_id, available_seats, ride_start_date, seat_price_amount, seat_price_currency, status, start_location_id, end_location_id) VALUES
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440001', 3, '2026-05-18T07:30:00', 18.00, 'EUR', 'CANCELLED', '22222222-2222-2222-2222-222222222221', '11111111-1111-1111-1111-111111111111')
ON CONFLICT (ride_id) DO NOTHING;

-- Insert bookings
INSERT INTO booking (booking_id, ride_id, passenger_id, payment_id, status) VALUES
('f0000000-0000-0000-0000-000000000001', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '660e8400-e29b-41d4-a716-446655440001', 'f1000000-0000-0000-0000-000000000001', 'CONFIRMED')
ON CONFLICT (booking_id) DO NOTHING;

INSERT INTO booking (booking_id, ride_id, passenger_id, payment_id, status) VALUES
('f0000000-0000-0000-0000-000000000002', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '770e8400-e29b-41d4-a716-446655440006', 'f1000000-0000-0000-0000-000000000002', 'CONFIRMED')
ON CONFLICT (booking_id) DO NOTHING;

INSERT INTO booking (booking_id, ride_id, passenger_id, payment_id, status) VALUES
('f0000000-0000-0000-0000-000000000003', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '660e8400-e29b-41d4-a716-446655440001', null, 'PENDING')
ON CONFLICT (booking_id) DO NOTHING;

INSERT INTO booking (booking_id, ride_id, passenger_id, payment_id, status) VALUES
('f0000000-0000-0000-0000-000000000004', 'cccccccc-cccc-cccc-cccc-cccccccccccc', '880e8400-e29b-41d4-a716-446655440007', 'f1000000-0000-0000-0000-000000000003', 'CONFIRMED')
ON CONFLICT (booking_id) DO NOTHING;

INSERT INTO booking (booking_id, ride_id, passenger_id, payment_id, status) VALUES
('f0000000-0000-0000-0000-000000000005', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '990e8400-e29b-41d4-a716-446655440008', null, 'CANCELLED')
ON CONFLICT (booking_id) DO NOTHING;
