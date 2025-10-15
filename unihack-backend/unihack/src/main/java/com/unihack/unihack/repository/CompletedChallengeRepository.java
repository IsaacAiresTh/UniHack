package com.unihack.unihack.repository;

import com.unihack.unihack.models.CompletedChallenge;
import com.unihack.unihack.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompletedChallengeRepository extends JpaRepository<CompletedChallenge, Long> {
    // Método para encontrar todos os desafios completados por um usuário específico
    List<CompletedChallenge> findByUser(User user);

    // Contar quantos desafios um usuário completou
    long countByUser(User user);
}