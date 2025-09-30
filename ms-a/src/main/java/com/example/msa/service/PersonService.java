package com.example.msa.service;

import com.example.dtos.PersonCreatedEvent;
import com.example.msa.kafka.KafkaProducer;
import com.example.msa.model.Person;
import com.example.msa.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonService {

    private final PersonRepository personRepository;
    private final KafkaProducer kafkaProducer;

    @Transactional
    public Person createPerson(Person person) {
        Person savedPerson = personRepository.save(person);
        log.info("Person created with ID: {}", savedPerson.getId());

        PersonCreatedEvent event = new PersonCreatedEvent(
                savedPerson.getId(),
                savedPerson.getDateNaissance()
        );
        kafkaProducer.sendPersonCreatedEvent(event);

        return savedPerson;
    }

    public Optional<Person> getPersonById(UUID id) {
        return personRepository.findById(id);
    }
}