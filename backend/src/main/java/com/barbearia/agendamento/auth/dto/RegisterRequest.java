package com.barbearia.agendamento.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, message = "A senha deve ter ao menos 8 caracteres")
        String senha,

        @NotBlank(message = "O telefone é obrigatório")
        @Pattern(
                regexp = "\\+?[0-9()\\-\\s]{8,20}",
                message = "Telefone inválido (use apenas números, com DDD)")
        String telefone
) {
}
