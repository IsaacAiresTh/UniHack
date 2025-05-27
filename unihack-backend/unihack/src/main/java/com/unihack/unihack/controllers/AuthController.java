package com.unihack.unihack.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unihack.unihack.dtos.RegisterDto;
import com.unihack.unihack.dtos.LoginDto;
import com.unihack.unihack.models.User;
import com.unihack.unihack.repository.UserRepository;

import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.validation.Valid;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Injetar o codificador de senha

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterDto registerDto) {
        if (userRepository.existsByMatricula(registerDto.getMatricula())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "User already exists"));
        } else {
            String encodedPassword = passwordEncoder.encode(registerDto.getPassword()); // Codificar a senha
            User newUser = new User(registerDto.getName(), registerDto.getMatricula(), encodedPassword);
            userRepository.save(newUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User registered successfully"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginDto loginDto) {
        Optional<User> user = userRepository.findByMatricula(loginDto.getMatricula());
        if (user != null && passwordEncoder.matches(loginDto.getPassword(), user.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "User logged in successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }
    }
}
