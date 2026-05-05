#!/bin/bash

curl -X 'GET' \
	'http://localhost:8083/bookings/f0000000-0000-0000-0000-000000000001' \
	-H 'accept: */*'
