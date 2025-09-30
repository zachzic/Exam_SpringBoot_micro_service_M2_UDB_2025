package com.example.msb.kafka;

import com.example.dtos.PersonCreatedEvent;
import com.example.msb.service.AgeCalculatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

    private final AgeCalculatorService ageCalculatorService;

    @KafkaListener(topics = "person-created-topic", groupId = "ms-b-group")
    public void handlePersonCreatedEvent(PersonCreatedEvent event) {
        log.info("Received PersonCreatedEvent for person: {}", event.getPersonId());
        ageCalculatorService.processPersonCreatedEvent(event);
    }
}