package com.barbearia.agendamento.appointment.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

public record CreateAppointmentRequest(
        @NotNull(message = "A data é obrigatória")
        LocalDate data,

        @NotNull(message = "O horário é obrigatório")
        LocalTime hora
) {
}
