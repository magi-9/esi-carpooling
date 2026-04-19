package com.esi.payment.exception;

public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(String message) { super(message); }
}
