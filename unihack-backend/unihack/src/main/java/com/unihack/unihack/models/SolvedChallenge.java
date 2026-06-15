package com.unihack.unihack.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "solved_challenges",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "challenge_id"})
)
public class SolvedChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Column(name = "solved_at", nullable = false)
    private LocalDateTime solvedAt;

    public SolvedChallenge(User user, Challenge challenge) {
        this.user = user;
        this.challenge = challenge;
        this.solvedAt = LocalDateTime.now();
    }
}
