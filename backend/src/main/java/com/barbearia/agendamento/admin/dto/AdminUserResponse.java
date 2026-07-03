package com.barbearia.agendamento.admin.dto;

import com.barbearia.agendamento.user.Role;
import com.barbearia.agendamento.user.User;

public record AdminUserResponse(
        Long id,
        String nome,
        String email,
        String telefone,
        Role role
) {
    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.getId(), user.getNome(), user.getEmail(), user.getTelefone(), user.getRole());
    }
}
