package com.lukianchykov.bookingsystem.controller.exception;

public class InvalidBookingStatusException extends BusinessLogicException {

    public InvalidBookingStatusException(String status, String operation) {
        super("Cannot " + operation + " booking with status: " + status);
    }
}