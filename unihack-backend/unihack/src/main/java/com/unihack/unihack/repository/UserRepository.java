package com.unihack.unihack.repository;
import com.unihack.unihack.models.User;


import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}