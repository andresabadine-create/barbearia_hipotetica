package com.barbearia.agendamento.appointment;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private LocalTime hora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status;

    /** Dono do agendamento (FK para users.id). */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    protected Appointment() {
        // exigido pelo JPA
    }

    public Appointment(LocalDate data, LocalTime hora, Long userId) {
        this.data = data;
        this.hora = hora;
        this.userId = userId;
        this.status = AppointmentStatus.AGENDADO;
    }

    public void cancelar() {
        this.status = AppointmentStatus.CANCELADO;
    }

    /** Marca o atendimento como concluído — confirmação do admin que habilita a fidelidade. */
    public void concluir() {
        this.status = AppointmentStatus.CONCLUIDO;
    }

    public boolean isCancelado() {
        return this.status == AppointmentStatus.CANCELADO;
    }

    public boolean isAgendado() {
        return this.status == AppointmentStatus.AGENDADO;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getData() {
        return data;
    }

    public LocalTime getHora() {
        return hora;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public Long getUserId() {
        return userId;
    }
}
