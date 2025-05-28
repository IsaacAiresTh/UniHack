package com.unihack.unihack.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RegisterDto {

    @NotBlank(message = "O nome não pode ser vazio")
    private String name;

    @NotBlank(message = "A matricula não pode ser vazia")
    @Size(min = 7, max = 7, message = "A matricula deve ter exatamente 7 caracteres")
    // Se a matrícula tiver um formato específico (ex: só números), você pode adicionar:
    @Pattern(regexp = "\\d{7}", message = "A matrícula deve conter apenas 7 dígitos")
    private String matricula; // Mantido como String para consistência e melhor validação de tamanho/formato

    @NotBlank(message = "A senha não pode ser vazia")
    @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres")
    private String password;

    // Considere adicionar um campo para confirmação de senha, se a lógica de negócio exigir:
    // @NotBlank(message = "A confirmação de senha não pode ser vazia")
    // private String confirmPassword;
    // A validação para checar se password e confirmPassword são iguais
    // geralmente é feita no serviço ou com uma anotação de validação customizada em nível de classe.
}