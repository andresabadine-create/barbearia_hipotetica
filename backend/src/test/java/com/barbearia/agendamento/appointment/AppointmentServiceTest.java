package com.barbearia.agendamento.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.barbearia.agendamento.common.exception.BadRequestException;
import com.barbearia.agendamento.common.exception.ConflictException;
import com.barbearia.agendamento.common.exception.NotFoundException;
import com.barbearia.agendamento.notification.NotificationService;

class AppointmentServiceTest {

    // "Agora" fixo: 30/06/2026 12:00 (UTC).
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-30T12:00:00Z"), ZoneOffset.UTC);

    private static final Long USER_ID = 1L;
    private static final LocalDate DATA_FUTURA = LocalDate.of(2026, 7, 1);
    private static final LocalTime HORA = LocalTime.of(10, 0);

    private AppointmentRepository repository;
    private NotificationService notifications;
    private AppointmentService service;

    @BeforeEach
    void setUp() {
        repository = mock(AppointmentRepository.class);
        notifications = mock(NotificationService.class);
        service = new AppointmentService(repository, notifications, clock);
    }

    @Test
    void criar_comHorarioNoPassado_lancaBadRequest() {
        LocalDate ontem = LocalDate.of(2026, 6, 29);

        assertThrows(BadRequestException.class,
                () -> service.criar(USER_ID, ontem, HORA));

        verify(repository, never()).save(any());
        verify(notifications, never()).notificar(any(), anyString());
    }

    @Test
    void criar_comVagaJaOcupada_lancaConflict() {
        when(repository.existsByDataAndHoraAndStatus(eq(DATA_FUTURA), eq(HORA), eq(AppointmentStatus.AGENDADO)))
                .thenReturn(true);

        assertThrows(ConflictException.class,
                () -> service.criar(USER_ID, DATA_FUTURA, HORA));

        verify(repository, never()).save(any());
    }

    @Test
    void criar_comVagaLivre_salvaAgendamento() {
        when(repository.existsByDataAndHoraAndStatus(any(), any(), eq(AppointmentStatus.AGENDADO)))
                .thenReturn(false);
        when(repository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment criado = service.criar(USER_ID, DATA_FUTURA, HORA);

        assertEquals(USER_ID, criado.getUserId());
        assertEquals(AppointmentStatus.AGENDADO, criado.getStatus());
        verify(repository).save(any(Appointment.class));
        verify(notifications).notificar(eq(USER_ID), anyString());
    }

    @Test
    void criar_quandoVagaSoTemCancelado_permiteReuso() {
        // Uma vaga cujo agendamento anterior foi cancelado NÃO conta como ocupada.
        when(repository.existsByDataAndHoraAndStatus(any(), any(), eq(AppointmentStatus.AGENDADO)))
                .thenReturn(false);
        when(repository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment criado = service.criar(USER_ID, DATA_FUTURA, HORA);

        assertEquals(AppointmentStatus.AGENDADO, criado.getStatus());
        verify(repository).save(any(Appointment.class));
    }

    @Test
    void cancelar_deAgendamentoDeOutroUsuario_lancaNotFound() {
        when(repository.findByIdAndUserId(99L, USER_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.cancelar(USER_ID, 99L));

        verify(repository, never()).save(any());
    }

    @Test
    void cancelar_doProprioUsuario_marcaComoCancelado() {
        Appointment agendamento = new Appointment(DATA_FUTURA, HORA, USER_ID);
        when(repository.findByIdAndUserId(5L, USER_ID)).thenReturn(Optional.of(agendamento));
        when(repository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment resultado = service.cancelar(USER_ID, 5L);

        assertEquals(AppointmentStatus.CANCELADO, resultado.getStatus());
        verify(repository).save(agendamento);
        verify(notifications).notificar(eq(USER_ID), anyString());
    }

    @Test
    void listar_retornaSomenteOsDoUsuario() {
        List<Appointment> esperado = List.of(new Appointment(DATA_FUTURA, HORA, USER_ID));
        when(repository.findByUserIdOrderByDataAscHoraAsc(USER_ID)).thenReturn(esperado);

        List<Appointment> resultado = service.listarDoUsuario(USER_ID);

        assertSame(esperado, resultado);
        verify(repository).findByUserIdOrderByDataAscHoraAsc(USER_ID);
    }
}
