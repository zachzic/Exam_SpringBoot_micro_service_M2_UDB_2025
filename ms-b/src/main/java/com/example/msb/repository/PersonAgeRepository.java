package com.example.msb.repository;

import com.example.msb.model.PersonAge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PersonAgeRepository extends JpaRepository<PersonAge, UUID> {
}