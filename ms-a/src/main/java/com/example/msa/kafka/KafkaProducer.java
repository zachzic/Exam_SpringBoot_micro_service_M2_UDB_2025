package com.example.msa.kafka;

import com.example.dtos.PersonCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPersonCreatedEvent(PersonCreatedEvent event) {
        log.info("Sending PersonCreatedEvent for person: {}", event.getPersonId());
        kafkaTemplate.send("person-created-topic", event.getPersonId().toString(), event);
    }
}