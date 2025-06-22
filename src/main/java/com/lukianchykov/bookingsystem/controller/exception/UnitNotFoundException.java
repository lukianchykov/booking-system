package com.lukianchykov.bookingsystem.controller.exception;

public class UnitNotFoundException extends ResourceNotFoundException {

    public UnitNotFoundException(Long unitId) {
        super("Unit not found with ID: " + unitId);
    }
}