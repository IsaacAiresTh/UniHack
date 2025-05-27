package com.unihack.unihack.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.UUID;

import com.unihack.unihack.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
public class User {



    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Username is required")
    @Column(unique = true, nullable = false)
    private String username;

    @NotNull(message = "matricula is required")
    @Column(nullable = false)
    @Pattern(regexp = "\\d{7}", message = "Matricula deve conter exatos 7 números")
    private String matricula;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    @Min(value = 0, message = "Points must be non-negative")
    @Column(nullable = false)
    private int points = 0;

    // Getters and Setters

    public User(String name, @NotNull(message = "A matricula não pode ser nulo") String matricula, String password) {
        this.username = name;
        this.matricula = matricula;
        this.password = password;
    }


}
