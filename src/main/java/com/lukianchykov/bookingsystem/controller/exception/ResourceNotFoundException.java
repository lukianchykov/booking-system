package com.lukianchykov.bookingsystem.controller.exception;

public class ResourceNotFoundException extends BookingSystemException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}