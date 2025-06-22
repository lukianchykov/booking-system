package com.lukianchykov.bookingsystem.utils;

import org.springframework.context.ApplicationEvent;

public class AvailableUnitsChangedEvent extends ApplicationEvent {

    public AvailableUnitsChangedEvent(Object source) {
        super(source);
    }
}
