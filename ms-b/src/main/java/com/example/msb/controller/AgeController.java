package com.example.msb.controller;

import com.example.msb.model.PersonAge;
import com.example.msb.service.AgeCalculatorService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/ages")
@RequiredArgsConstructor
@Tag(name = "Ages", description = "Consultation des ages calcules")
public class AgeController {

    private final AgeCalculatorService ageCalculatorService;

    @GetMapping("/{personId}")
    @Operation(
            summary = "Recuperer l'age calcule d'une personne",
            description = "Retourne les informations d'age calculees par MS-B.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Age trouve",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PersonAge.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "personId": "550e8400-e29b-41d4-a716-446655440000",
                                      "dateNaissance": "1990-05-15",
                                      "age": 35,
                                      "calculatedAt": "2026-02-16"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Age introuvable",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-02-16T10:15:30Z",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Age introuvable",
                                      "path": "/ages/550e8400-e29b-41d4-a716-446655440001"
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<?> getPersonAge(@PathVariable UUID personId) {
        PersonAge personAge = ageCalculatorService.getPersonAge(personId).orElse(null);
        if (personAge == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundBody("Age introuvable", "/ages/" + personId));
        }
        return ResponseEntity.ok(personAge);
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
