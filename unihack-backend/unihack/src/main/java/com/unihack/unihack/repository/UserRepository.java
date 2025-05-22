package com.unihack.unihack.repository;
import com.unihack.unihack.models.User;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByMatricula(int matricula);

    User findByMatricula(int matricula);

    List<User> findAllByOrderByPointsDesc();
}