package com.unihack.unihack.dtos;

import com.unihack.unihack.models.User;

public class UserProfileDTO {

    private String username;
    private String matricula;
    private int points;
    private UserStatsDTO stats;

    public UserProfileDTO(User user) {
        this.username = user.getUsername();
        this.matricula = user.getMatricula();
        this.points = user.getPoints();
    }

    // Getters e Setters (incluindo para 'stats')

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public UserStatsDTO getStats() {
        return stats;
    }

    public void setStats(UserStatsDTO stats) {
        this.stats = stats;
    }
}