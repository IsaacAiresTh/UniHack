package com.unihack.unihack.dtos;

import com.unihack.unihack.models.User;
import lombok.Getter;

import java.util.UUID;

@Getter
public class RankingUserDto {

    private final UUID id;
    private final String username;
    private final String matricula;
    private final String role;
    private final int points;

    public RankingUserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.matricula = user.getMatricula();
        this.role = user.getRole().name();
        this.points = user.getPoints();
    }
}
