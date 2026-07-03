package com.barbearia.agendamento.admin.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

/** Aviso do admin para destinatários específicos. */
public record AvisoRequest(
        @NotBlank(message = "A mensagem é obrigatória")
        String mensagem,

        @NotEmpty(message = "Selecione ao menos um destinatário")
        List<Long> destinatarioIds
) {
}
