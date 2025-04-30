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

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, String> user) {
        // Logic to register the user
        // Save the user to the database using userRepository
        // Return a response indicating success or failure
        // Validation logic can be added here if needed

        if (userRepository.existsByEmail(user.get("email"))) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "User already exists"));
        } else {
            User newUser = new User(user.get("name"), user.get("email"), user.get("password"));
            userRepository.save(newUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User registered successfully"));

        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> user) {
        // Logic to authenticate the user
        // Check if the user exists in the database using userRepository
        // Return a response indicating success or failure
        // Removed unused result variable and related block

        
        if (userRepository.existsByEmail(user.get("email"))) {
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "User logged in successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }
    }

}
