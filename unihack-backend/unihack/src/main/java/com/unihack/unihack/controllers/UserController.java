package com.unihack.unihack.controllers;

import com.unihack.unihack.models.User;
import com.unihack.unihack.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/ranking")
    public List<User> getUsersByRanking() {
        return userRepository.findAllByOrderByPointsDesc();
    }
}
