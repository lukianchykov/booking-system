package com.lukianchykov.bookingsystem.controller.exception;

public class BookingNotFoundException extends ResourceNotFoundException {

    public BookingNotFoundException(Long bookingId) {
        super("Booking not found with ID: " + bookingId);
    }
}