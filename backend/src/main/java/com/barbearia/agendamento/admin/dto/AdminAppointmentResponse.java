package com.barbearia.agendamento.admin.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.barbearia.agendamento.appointment.Appointment;

/** Agendamento aguardando confirmação de conclusão, com o nome do cliente. */
public record AdminAppointmentResponse(
        Long id,
        Long userId,
        String clienteNome,
        LocalDate data,
        LocalTime hora
) {
    public static AdminAppointmentResponse from(Appointment a, String clienteNome) {
        return new AdminAppointmentResponse(a.getId(), a.getUserId(), clienteNome, a.getData(), a.getHora());
    }
}
