package com.example.msa.controller;

import com.example.msa.model.Person;
import com.example.msa.service.PersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/persons")
@RequiredArgsConstructor
@Tag(name = "Persons", description = "Gestion des personnes")
public class PersonController {

    private final PersonService personService;

    @PostMapping
    @Operation(
            summary = "Creer une personne",
            description = "Cree une personne puis declenche le calcul d'age via Kafka.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Personne creee",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Person.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": "550e8400-e29b-41d4-a716-446655440000",
                                      "nom": "Durand",
                                      "prenom": "Marie",
                                      "adresse": "123 rue de la Paix",
                                      "dateNaissance": "1990-05-15",
                                      "telephone": "0612345678",
                                      "statut": "EN_ATTENTE",
                                      "age": null
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<Person> createPerson(@RequestBody Person person) {
        Person createdPerson = personService.createPerson(person);
        return new ResponseEntity<>(createdPerson, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Recuperer une personne par ID",
            description = "Retourne la personne et son statut de traitement.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Personne trouvee",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Person.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": "550e8400-e29b-41d4-a716-446655440000",
                                      "nom": "Durand",
                                      "prenom": "Marie",
                                      "adresse": "123 rue de la Paix",
                                      "dateNaissance": "1990-05-15",
                                      "telephone": "0612345678",
                                      "statut": "TERMINE",
                                      "age": 35
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Personne introuvable",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-02-16T10:15:30Z",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Personne introuvable",
                                      "path": "/persons/550e8400-e29b-41d4-a716-446655440001"
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<?> getPersonById(@PathVariable UUID id) {
        Person person = personService.getPersonById(id).orElse(null);
        if (person == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundBody("Personne introuvable", "/persons/" + id));
        }
        return ResponseEntity.ok(person);
    }

    private Map<String, Object> notFoundBody(String message, String path) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", HttpStatus.NOT_FOUND.getReasonPhrase());
        body.put("message", message);
        body.put("path", path);
        return body;
    }
}
