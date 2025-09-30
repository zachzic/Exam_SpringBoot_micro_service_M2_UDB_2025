package com.example.msb.service;

import com.example.dtos.AgeEvent;
import com.example.dtos.PersonCreatedEvent;
import com.example.msb.model.PersonAge;
import com.example.msb.repository.PersonAgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgeCalculatorService {

    private final PersonAgeRepository personAgeRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void processPersonCreatedEvent(PersonCreatedEvent event) {
        try {
            int age = calculateAge(event.getDateDeNaissance());

            PersonAge personAge = new PersonAge();
            personAge.setPersonId(event.getPersonId());
            personAge.setDateNaissance(event.getDateDeNaissance());
            personAge.setAge(age);
            personAge.setCalculatedAt(LocalDate.now());

            personAgeRepository.save(personAge);
            log.info("Saved PersonAge for person: {} with age: {}", event.getPersonId(), age);

            AgeEvent ageEvent = new AgeEvent(event.getPersonId(), age, "TERMINE");
            kafkaTemplate.send("age-calculated-topic", event.getPersonId().toString(), ageEvent);
            log.info("Sent AgeEvent for person: {}", event.getPersonId());

        } catch (Exception e) {
            log.error("Error processing PersonCreatedEvent for person: {}", event.getPersonId(), e);
            AgeEvent ageEvent = new AgeEvent(event.getPersonId(), null, "ECHEC");
            kafkaTemplate.send("age-calculated-topic", event.getPersonId().toString(), ageEvent);
        }
    }

    private int calculateAge(LocalDate dateNaissance) {
        return Period.between(dateNaissance, LocalDate.now()).getYears();
    }

    public Optional<PersonAge> getPersonAge(UUID personId) {
        return personAgeRepository.findById(personId);
    }
}