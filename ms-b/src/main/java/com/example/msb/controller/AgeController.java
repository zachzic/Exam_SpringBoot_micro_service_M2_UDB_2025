package com.example.msb.controller;

import com.example.msb.model.PersonAge;
import com.example.msb.service.AgeCalculatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/ages")
@RequiredArgsConstructor
public class AgeController {

    private final AgeCalculatorService ageCalculatorService;

    @GetMapping("/{personId}")
    public ResponseEntity<PersonAge> getPersonAge(@PathVariable UUID personId) {
        return ageCalculatorService.getPersonAge(personId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}