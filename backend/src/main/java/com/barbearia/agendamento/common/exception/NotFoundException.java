package com.barbearia.agendamento.common.exception;

/** Recurso inexistente ou fora do escopo do usuário → HTTP 404. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
