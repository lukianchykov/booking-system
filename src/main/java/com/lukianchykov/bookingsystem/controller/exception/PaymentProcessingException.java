package com.lukianchykov.bookingsystem.controller.exception;

public class PaymentProcessingException extends BusinessLogicException {

    public PaymentProcessingException(String status) {
        super("Cannot process payment for booking with status: " + status);
    }
}