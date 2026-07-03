package com.barbearia.agendamento.auth.dto;

import com.barbearia.agendamento.user.Role;

public record AuthResponse(
        String token,
        Long userId,
        String nome,
        String email,
        Role role
) {
}
