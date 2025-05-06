package com.unihack.unihack.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class ChallengeDto {
    private UUID id;
    private String name;
    private String description;
    private String difficulty;

}