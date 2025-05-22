package com.unihack.unihack.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginDto {

    // Getters e Setterr
    @NotNull(message = "A matricula não pode ser nulo")
    private int matricula;

    @NotNull(message = "A senha não pode ser nula")
    private String password;

}
