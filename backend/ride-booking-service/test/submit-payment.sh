#!/bin/bash

curl -X 'POST' \
	'http://localhost:8081/payments' \
	-H 'accept: */*' \
	-H 'Content-Type: application/json' \
	-d '{
  "bookingId": "f0000000-0000-0000-0000-000000000001",
  "payerId": "660e8400-e29b-41d4-a716-446655440001",
  "payeeId": "660e8400-e29b-41d4-a716-446655440001",
  "amount": 20,
  "currency": "EUR"
}'
