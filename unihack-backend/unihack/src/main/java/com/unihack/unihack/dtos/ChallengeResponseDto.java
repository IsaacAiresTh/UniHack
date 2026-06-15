package com.unihack.unihack.dtos;

import com.unihack.unihack.models.Challenge;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ChallengeResponseDto {

    private final UUID id;
    private final String title;
    private final String description;
    private final String difficulty;
    private final int score;
    private final String slug;
    private final String category;

    public ChallengeResponseDto(Challenge challenge) {
        this.id = challenge.getId();
        this.title = challenge.getTitle();
        this.description = challenge.getDescription();
        this.difficulty = challenge.getDifficulty();
        this.score = challenge.getScore();
        this.slug = challenge.getSlug();
        this.category = challenge.getCategory();
    }
}
