package com.unihack.unihack.dtos;

import java.util.UUID;

public class UserDto {
    private UUID id;

    @NotNull(message = "O nome não pode ser nulo")
    private String name;

    @email(message = "O email deve ser válido")
    @NotNull(message = "O email não pode ser nulo")
    private String email;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}