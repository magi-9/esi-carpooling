#!/bin/bash
# Setup test users, rides and reviews for Checkpoint 2 demo
# Usage: ./scripts/setup-test-users.sh

set -e

RDB="docker exec esi-carpooling-postgre_db-1 psql -U postgres -d esi2023 -c"
PDB="docker exec -e PGPASSWORD=WG2RwqeUXveDWnheYdCwu9AFZaMUf37m esi-profile-db psql -U AbEm3dXZ87Xsq7Y3 -d profile_db -c"
REVDB="docker exec esi-carpooling-review-service-db-1 psql -U reviews -d reviews -c"

echo "=== Create users via auth API ==="
register_or_login() {
	local email=$1
	local password=$2
	local roles=$3

	local resp=$(curl -s -X POST http://localhost:8086/api/auth/register \
		-H "Content-Type: application/json" \
		-d "{\"email\":\"$email\",\"password\":\"$password\",\"roles\":$roles}")
	if echo "$resp" | python3 -c "import sys,json; json.load(sys.stdin)" 2>/dev/null | grep -q "token"; then
		echo "$resp" | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])"
	else
		curl -s -X POST http://localhost:8086/api/auth/login \
			-H "Content-Type: application/json" \
			-d "{\"email\":\"$email\",\"password\":\"$password\"}" |
			python3 -c "import sys,json; print(json.load(sys.stdin)['token'])"
	fi
	local email=$1
	local password=$2
	local roles=$3
	local resp=$(curl -s -X POST http://localhost:8086/api/auth/register \
		-H "Content-Type: application/json" \
		-d "{\"email\":\"$email\",\"password\":\"$password\",\"roles\":$roles}")
	if echo "$resp" | python3 -c "import sys,json; json.load(sys.stdin)" 2>/dev/null | grep -q "token"; then
		echo "$resp" | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])"
	else
		curl -s -X POST http://localhost:8086/api/auth/login \
			-H "Content-Type: application/json" \
			-d "{\"email\":\"$email\",\"password\":\"$password\"}" |
			python3 -c "import sys,json; print(json.load(sys.stdin)['token'])"
	fi
}

DRIVER_TOKEN=$(register_or_login "driver@test.com" "password123" '["DRIVER"]')
PASS_TOKEN=$(register_or_login "passenger@test.com" "password123" '["PASSENGER"]')

DRIVER_ID=$(echo "$DRIVER_TOKEN" | python3 -c "
import sys,base64,json
p=sys.stdin.read().split('.')[1]
while len(p) % 4: p+='='
print(json.loads(base64.urlsafe_b64decode(p))['sub'])
")
PASSENGER_ID=$(echo "$PASS_TOKEN" | python3 -c "
import sys,base64,json
p=sys.stdin.read().split('.')[1]
while len(p) % 4: p+='='
print(json.loads(base64.urlsafe_b64decode(p))['sub'])
")

echo "Driver: $DRIVER_ID"
echo "Passenger: $PASSENGER_ID"

echo ""
echo "=== Create ride locations ==="
$RDB "
INSERT INTO ride_location (ride_location_id, display_address, latitude, longitude) VALUES
  ('a1111111-1111-1111-1111-111111111111', 'Tallinn', 59.437242, 24.7572693),
  ('b1111111-1111-1111-1111-111111111111', 'Vilnius', 54.6870458, 25.2829111),
  ('c1111111-1111-1111-1111-111111111111', 'Kaunas', 54.878744, 24.860185),
  ('d1111111-1111-1111-1111-111111111111', 'Tartu', 58.3779528, 26.7290023),
  ('e1111111-1111-1111-1111-111111111111', 'Pärnu', 58.385954, 24.497533)
ON CONFLICT DO NOTHING;
"

echo ""
echo "=== Create verified vehicle (profile DB) ==="
$PDB "
INSERT INTO vehicles (vehicle_id, user_id, make, model, license_plate, verification_status)
VALUES ('11111111-1111-1111-1111-111111111111', '$DRIVER_ID', 'Toyota', 'Camry', 'ABC-123', 'SUCCESS')
ON CONFLICT DO NOTHING;
"

echo ""
echo "=== Create 3 rides ==="
$RDB "
INSERT INTO ride (ride_id, available_seats, driver_id, ride_start_date, seat_price_amount, seat_price_currency, status, vehicle_id, start_location_id, end_location_id) VALUES
  ('d1111111-d111-1111-1111-111111111111', 3, '$DRIVER_ID', '2026-05-20 10:00:00', 25.00, 'EUR', 'PENDING', '11111111-1111-1111-1111-111111111111', 'a1111111-1111-1111-1111-111111111111', 'b1111111-1111-1111-1111-111111111111'),
  ('e1111111-e111-1111-1111-111111111111', 2, '$DRIVER_ID', '2026-05-21 14:00:00', 15.00, 'EUR', 'PENDING', '11111111-1111-1111-1111-111111111111', 'b1111111-1111-1111-1111-111111111111', 'c1111111-1111-1111-1111-111111111111'),
  ('f1111111-f111-1111-1111-111111111111', 4, '$DRIVER_ID', '2026-05-10 08:00:00', 20.00, 'EUR', 'COMPLETED', '11111111-1111-1111-1111-111111111111', 'a1111111-1111-1111-1111-111111111111', 'c1111111-1111-1111-1111-111111111111')
ON CONFLICT DO NOTHING;
"

echo ""
echo "=== Create completed booking for passenger ==="
$RDB "
INSERT INTO booking (booking_id, ride_id, passenger_id, payment_id, status)
VALUES ('b1111111-b111-1111-1111-111111111111', 'f1111111-f111-1111-1111-111111111111', '$PASSENGER_ID', NULL, 'COMPLETED')
ON CONFLICT DO NOTHING;
"

echo ""
echo "=== Create 2 reviews for completed ride (passenger review will be created during demo) ==="
$REVDB "
INSERT INTO review (review_id, booking_id, ride_id, reviewer_id, comment, stars, created_at, deleted) VALUES
  ('c1111111-c111-1111-1111-111111111111', 'c0000000-0000-0000-0000-000000000002', 'f1111111-f111-1111-1111-111111111111', '660e8400-e29b-41d4-a716-446655440001', 'Smooth ride, would recommend!', 5, '2026-05-10 14:00:00', false),
  ('c2222222-c222-2222-2222-222222222222', 'c0000000-0000-0000-0000-000000000003', 'f1111111-f111-1111-1111-111111111111', '770e8400-e29b-41d4-a716-446655440006', 'Good driver but slightly late', 4, '2026-05-10 15:30:00', false)
ON CONFLICT DO NOTHING;
"

echo ""
echo "=== Done ==="
echo "driver@test.com / password123 (ID: $DRIVER_ID)"
echo "  - 3 rides (2 pending, 1 completed)"
echo "  - vehicle: Toyota Camry verified"
echo ""
echo "passenger@test.com / password123 (ID: $PASSENGER_ID)"
echo "  - 1 completed booking with review"
