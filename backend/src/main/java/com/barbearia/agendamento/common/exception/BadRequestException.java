package com.barbearia.agendamento.common.exception;

/** Requisição inválida por regra de negócio (ex.: agendar no passado) → HTTP 400. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
