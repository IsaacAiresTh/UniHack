package com.unihack.unihack.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "completed_challenges")
public class CompletedChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    // Construtores
    public CompletedChallenge() {
    }

    public CompletedChallenge(User user, Challenge challenge) {
        this.user = user;
        this.challenge = challenge;
        this.completedAt = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}