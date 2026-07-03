package com.barbearia.agendamento.appointment.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.barbearia.agendamento.appointment.Appointment;
import com.barbearia.agendamento.appointment.AppointmentStatus;

public record AppointmentResponse(
        Long id,
        LocalDate data,
        LocalTime hora,
        AppointmentStatus status
) {
    public static AppointmentResponse from(Appointment a) {
        return new AppointmentResponse(a.getId(), a.getData(), a.getHora(), a.getStatus());
    }
}
