package com.unihack.unihack.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unihack.unihack.models.User;
import com.unihack.unihack.repository.UserRepository;

import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Injetar o codificador de senha

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, String> user) {
        if (userRepository.existsByEmail(user.get("email"))) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "User already exists"));
        } else {
            String encodedPassword = passwordEncoder.encode(user.get("password")); // Codificar a senha
            User newUser = new User(user.get("name"), user.get("email"), encodedPassword);
            userRepository.save(newUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User registered successfully"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> user) {
        if (userRepository.existsByEmail(user.get("email"))) {
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "User logged in successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }
    }

}
