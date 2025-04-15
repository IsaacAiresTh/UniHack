package com.unihack.unihack.repository;

import com.unihack.unihack.models.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
}