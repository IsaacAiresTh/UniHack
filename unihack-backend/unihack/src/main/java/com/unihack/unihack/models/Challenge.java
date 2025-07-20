package com.unihack.unihack.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "challenges")
public class Challenge {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    @Column(length = 2048) // Aumenta o tamanho máximo da descrição
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Min(value = 10, message = "Score must be a positive number")
    private int score;

    // --- NOVOS CAMPOS ADICIONADOS ---

    @NotBlank(message = "Docker image is required")
    @Column(name = "docker_image") // Mapeia para a coluna 'docker_image' no banco
    private String dockerImage;

    @NotBlank(message = "Flag is required")
    private String flag;

    // --- FIM DOS NOVOS CAMPOS ---

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
}