package com.barbearia.agendamento.common.exception;

/** Violação de regra de unicidade (ex.: vaga já ocupada) → HTTP 409. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
