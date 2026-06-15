package com.unihack.unihack.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "challenges")
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Title is required")
    @Column(nullable = false, unique = true)
    private String title;

    @NotBlank(message = "Description is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Difficulty is required")
    @Column(nullable = false)
    private String difficulty;

    @Min(value = 10, message = "Score must be at least 10")
    @Column(nullable = false)
    private int score;

    @NotBlank(message = "Slug is required")
    @Column(nullable = false, unique = true)
    private String slug;

    @NotBlank(message = "Flag hash is required")
    @Column(name = "flag_hash", nullable = false)
    private String flagHash;

    @Column
    private String category;
}
