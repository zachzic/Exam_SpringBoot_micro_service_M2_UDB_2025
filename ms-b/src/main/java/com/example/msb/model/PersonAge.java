package com.example.msb.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "person_ages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonAge {
    @Id
    private UUID personId;

    @Column(nullable = false)
    private LocalDate dateNaissance;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    private LocalDate calculatedAt;
}