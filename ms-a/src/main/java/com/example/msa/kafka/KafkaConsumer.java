package com.example.msa.kafka;

import com.example.dtos.AgeEvent;
import com.example.msa.model.Status;
import com.example.msa.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

    private final PersonRepository personRepository;

    @KafkaListener(topics = "age-calculated-topic", groupId = "ms-a-group")
    public void handleAgeEvent(AgeEvent event) {
        log.info("Événement d'âge reçu pour la personne: {} avec l'âge: {}", event.getPersonId(), event.getAge());

        personRepository.findById(event.getPersonId()).ifPresent(person -> {
            person.setAge(event.getAge());
            person.setStatut("TERMINE".equals(event.getStatus()) ? Status.TERMINE : Status.ECHEC);
            personRepository.save(person);
            log.info("Personne mise à jour {} avec l'âge {} et le statut {}",
                    event.getPersonId(), event.getAge(), person.getStatut());
        });
    }
}