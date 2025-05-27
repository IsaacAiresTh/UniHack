package com.unihack.unihack.repository;
import com.unihack.unihack.models.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByMatricula(@NotNull @Pattern(regexp = "") String matricula);

    Optional<User> findByMatricula(String matricula);

    List<User> findAllByOrderByPointsDesc();
}