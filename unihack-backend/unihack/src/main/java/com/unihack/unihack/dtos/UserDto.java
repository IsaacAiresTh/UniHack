package com.unihack.unihack.dtos;

import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserDto {
    private UUID id;

    @NotNull(message = "O nome não pode ser nulo")
    private String name;

    @Email(message = "O email deve ser válido")
    @NotNull(message = "A matricula não pode ser nulo")
    private int matricula;

    @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres")
    private String password;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMatricula() {
        return matricula;
    }

    public void setMatricula(int matricula) {
        this.matricula = matricula;
    }

    
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}