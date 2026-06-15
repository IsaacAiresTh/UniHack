package com.unihack.unihack.repository;

import com.unihack.unihack.models.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChallengeRepository extends JpaRepository<Challenge, UUID> {

    Optional<Challenge> findByFlagHash(String flagHash);
}
