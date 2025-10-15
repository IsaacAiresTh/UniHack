package com.unihack.unihack.repository;

import com.unihack.unihack.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByMatricula(String matricula);

    // --- MÉTODO ADICIONADO ---
    // O Spring Security usa 'username' como padrão, então vamos mapeá-lo para a matrícula.
    Optional<User> findByUsername(String username);
    boolean existsByMatricula(String matricula);

    List<User> findAllByOrderByPointsDesc();
}