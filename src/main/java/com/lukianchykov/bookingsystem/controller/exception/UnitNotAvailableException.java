package com.lukianchykov.bookingsystem.controller.exception;

public class UnitNotAvailableException extends BusinessLogicException {

    public UnitNotAvailableException() {
        super("Unit is not available for the selected dates");
    }
}