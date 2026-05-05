#!/bin/bash

# Configuration
BOOKING_API="http://localhost:8083"
PAYMENT_API="http://localhost:8081"
BOOKING_ID="f0000000-0000-0000-0000-000000000001"

echo "=== Get all bookings ==="
curl -s -X 'GET' \
	"$BOOKING_API/bookings" \
	-H 'accept: */*'
echo -e "\n"

echo "=== Get individual booking ==="
curl -s -X 'GET' \
	"$BOOKING_API/bookings/$BOOKING_ID" \
	-H 'accept: */*'
echo -e "\n"

echo "=== Submit payment ==="
curl -s -X 'POST' \
	"$PAYMENT_API/payments" \
	-H 'accept: */*' \
	-H 'Content-Type: application/json' \
	-d '{
  "bookingId": "'$BOOKING_ID'",
  "payerId": "660e8400-e29b-41d4-a716-446655440001",
  "payeeId": "660e8400-e29b-41d4-a716-446655440001",
  "amount": 20,
  "currency": "EUR"
}'
echo -e "\n"
