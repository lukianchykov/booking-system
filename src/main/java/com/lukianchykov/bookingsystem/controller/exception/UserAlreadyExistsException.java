package com.lukianchykov.bookingsystem.controller.exception;

public class UserAlreadyExistsException extends BusinessLogicException {

    public UserAlreadyExistsException(String email) {
        super("User with email already exists: " + email);
    }
}