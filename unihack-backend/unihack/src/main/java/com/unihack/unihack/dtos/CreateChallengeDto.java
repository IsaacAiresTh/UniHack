package com.unihack.unihack.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateChallengeDto {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Difficulty is required")
    private String difficulty;

    @Min(value = 10, message = "Score must be at least 10")
    private int score;

    @NotBlank(message = "Slug is required")
    private String slug;

    @NotBlank(message = "Flag is required")
    private String flag;

    private String category;
}
