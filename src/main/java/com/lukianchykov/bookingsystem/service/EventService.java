package com.lukianchykov.bookingsystem.service;

import com.lukianchykov.bookingsystem.domain.Event;
import com.lukianchykov.bookingsystem.repository.EventRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService {

    private final EventRepository eventRepository;

    public void createEvent(String eventType, String entityType, Long entityId, String eventData) {
        Event event = Event.builder()
            .eventType(eventType)
            .entityType(entityType)
            .entityId(entityId)
            .eventData(eventData)
            .build();

        eventRepository.save(event);
    }
}