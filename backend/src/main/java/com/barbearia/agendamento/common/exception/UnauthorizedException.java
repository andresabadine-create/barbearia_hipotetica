package com.barbearia.agendamento.common.exception;

/** Falha de autenticação (credenciais inválidas) → HTTP 401. */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
