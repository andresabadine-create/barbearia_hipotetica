package com.barbearia.agendamento.appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /** Agendamentos de um usuário, mais próximos primeiro. */
    List<Appointment> findByUserIdOrderByDataAscHoraAsc(Long userId);

    /** Agendamentos de um usuário em um dado status (ex.: para o cartão fidelidade). */
    List<Appointment> findByUserIdAndStatus(Long userId, AppointmentStatus status);

    /** Agendamentos em um dado status, mais recentes primeiro (painel do admin). */
    List<Appointment> findByStatusOrderByDataDescHoraDesc(AppointmentStatus status);

    /** Verifica se a vaga (data + hora) já está ocupada por um agendamento ativo. */
    boolean existsByDataAndHoraAndStatus(LocalDate data, LocalTime hora, AppointmentStatus status);

    /** Busca um agendamento garantindo que pertence ao usuário informado. */
    Optional<Appointment> findByIdAndUserId(Long id, Long userId);
}
