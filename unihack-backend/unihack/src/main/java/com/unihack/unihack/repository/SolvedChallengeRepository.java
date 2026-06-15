package com.unihack.unihack.repository;

import com.unihack.unihack.models.Challenge;
import com.unihack.unihack.models.SolvedChallenge;
import com.unihack.unihack.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SolvedChallengeRepository extends JpaRepository<SolvedChallenge, UUID> {

    boolean existsByUserAndChallenge(User user, Challenge challenge);

    List<SolvedChallenge> findByUser(User user);

    long countByUser(User user);
}
