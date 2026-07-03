package com.barbearia.agendamento.common;

import java.time.OffsetDateTime;

/** Corpo padronizado de erro devolvido pela API. */
public record ApiError(
        int status,
        String error,
        String message,
        OffsetDateTime timestamp
) {
    public static ApiError of(int status, String error, String message) {
        return new ApiError(status, error, message, OffsetDateTime.now());
    }
}
