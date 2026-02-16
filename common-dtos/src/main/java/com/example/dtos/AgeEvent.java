package com.example.dtos;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgeEvent {
    private UUID personId;
    private Integer age;
    private String status; // TERMINE ou ECHEC
}
