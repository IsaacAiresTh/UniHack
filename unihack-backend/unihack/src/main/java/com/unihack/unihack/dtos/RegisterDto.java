package com.unihack.unihack.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RegisterDto {

    // Getters e Setters
    @NotNull(message = "O nome não pode ser nulo")
    private String name;

    @Email(message = "O email deve ser válido")
    @NotNull(message = "O email não pode ser nulo")
    private String email;

    @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres")
    private String password;

}
