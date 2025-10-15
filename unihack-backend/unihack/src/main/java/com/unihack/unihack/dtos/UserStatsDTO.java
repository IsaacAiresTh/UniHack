package com.unihack.unihack.dtos;

public class UserStatsDTO {

    private long completedChallenges;
    private long totalChallenges;
    private double progressPercentage;
    private String favoriteCategory;

    // Getters e Setters

    public long getCompletedChallenges() {
        return completedChallenges;
    }

    public void setCompletedChallenges(long completedChallenges) {
        this.completedChallenges = completedChallenges;
    }

    public long getTotalChallenges() {
        return totalChallenges;
    }

    public void setTotalChallenges(long totalChallenges) {
        this.totalChallenges = totalChallenges;
    }

    public double getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public String getFavoriteCategory() {
        return favoriteCategory;
    }

    public void setFavoriteCategory(String favoriteCategory) {
        this.favoriteCategory = favoriteCategory;
    }
}