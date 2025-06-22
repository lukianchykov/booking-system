package com.lukianchykov.bookingsystem.controller.exception;

public abstract class BookingSystemException extends RuntimeException {

    protected BookingSystemException(String message) {
        super(message);
    }
}