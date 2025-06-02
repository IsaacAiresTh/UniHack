package com.unihack.unihack.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
// RegisterDto.java
public class RegisterDto {
    @NotBlank(message = "O nome não pode ser vazio")
    private String name; // Espera 'name'

    @NotBlank(message = "A matricula não pode ser vazia")
    @Size(min = 7, max = 7, message = "A matricula deve ter exatamente 7 caracteres")
    @Pattern(regexp = "\\d{7}", message = "A matrícula deve conter apenas 7 dígitos")
    private String matricula;

    @NotBlank(message = "A senha não pode ser vazia")
    @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres")
    private String password; // Espera 'password'

    @NotBlank(message = "A confirmação de senha não pode ser vazia")
    private String confirmPassword; // Espera 'confirmPassword'
}