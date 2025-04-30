package com.unihack.unihack.repository;

import com.unihack.unihack.models.Challenge;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeRepository extends JpaRepository<Challenge, UUID> {
}