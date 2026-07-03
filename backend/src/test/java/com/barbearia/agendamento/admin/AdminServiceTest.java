package com.barbearia.agendamento.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.barbearia.agendamento.admin.dto.AdminAppointmentResponse;
import com.barbearia.agendamento.admin.dto.AdminUserResponse;
import com.barbearia.agendamento.appointment.Appointment;
import com.barbearia.agendamento.appointment.AppointmentRepository;
import com.barbearia.agendamento.appointment.AppointmentStatus;
import com.barbearia.agendamento.common.exception.BadRequestException;
import com.barbearia.agendamento.common.exception.NotFoundException;
import com.barbearia.agendamento.notification.NotificationService;
import com.barbearia.agendamento.user.Role;
import com.barbearia.agendamento.user.User;
import com.barbearia.agendamento.user.UserRepository;

class AdminServiceTest {

    // "Agora" fixo: 30/06/2026 12:00 (UTC).
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-30T12:00:00Z"), ZoneOffset.UTC);

    private static final LocalDate PASSADO = LocalDate.of(2026, 6, 29);
    private static final LocalDate FUTURO = LocalDate.of(2026, 7, 2);
    private static final LocalTime HORA = LocalTime.of(10, 0);

    private UserRepository users;
    private NotificationService notifications;
    private AppointmentRepository appointments;
    private AdminService service;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        notifications = mock(NotificationService.class);
        appointments = mock(AppointmentRepository.class);
        service = new AdminService(users, notifications, appointments, clock);
    }

    @Test
    void listarUsuarios_mapeiaCadastros() {
        User ana = new User("Ana", "ana@ex.com", "hash", "11999998888");
        when(users.findAll()).thenReturn(List.of(ana));

        List<AdminUserResponse> resultado = service.listarUsuarios();

        assertEquals(1, resultado.size());
        assertEquals("ana@ex.com", resultado.get(0).email());
        assertEquals(Role.USER, resultado.get(0).role());
    }

    @Test
    void enviarAviso_semDestinatarios_lancaBadRequest() {
        assertThrows(BadRequestException.class, () -> service.enviarAviso("Oi", List.of()));

        verify(notifications, never()).notificar(any(), anyString());
    }

    @Test
    void enviarAviso_comDestinatarioInexistente_lancaBadRequest() {
        User u1 = mock(User.class);
        when(u1.getId()).thenReturn(1L);
        // Pediu [1, 2] mas só 1 existe.
        when(users.findAllById(List.of(1L, 2L))).thenReturn(List.of(u1));

        assertThrows(BadRequestException.class,
                () -> service.enviarAviso("Promoção!", List.of(1L, 2L)));

        verify(notifications, never()).notificar(any(), anyString());
    }

    @Test
    void enviarAviso_comDestinatariosValidos_notificaCada() {
        User u1 = mock(User.class);
        User u2 = mock(User.class);
        when(u1.getId()).thenReturn(1L);
        when(u2.getId()).thenReturn(2L);
        when(users.findAllById(List.of(1L, 2L))).thenReturn(List.of(u1, u2));

        int enviados = service.enviarAviso("Fechado amanhã", List.of(1L, 2L));

        assertEquals(2, enviados);
        verify(notifications).notificar(eq(1L), eq("📢 Fechado amanhã"));
        verify(notifications).notificar(eq(2L), eq("📢 Fechado amanhã"));
    }

    @Test
    void listarAgendamentosPendentes_ignoraFuturosEMapeiaNome() {
        Appointment passado = new Appointment(PASSADO, HORA, 1L);
        Appointment futuro = new Appointment(FUTURO, HORA, 2L);
        when(appointments.findByStatusOrderByDataDescHoraDesc(AppointmentStatus.AGENDADO))
                .thenReturn(List.of(passado, futuro));
        User ana = mock(User.class);
        when(ana.getId()).thenReturn(1L);
        when(ana.getNome()).thenReturn("Ana");
        when(users.findAllById(List.of(1L))).thenReturn(List.of(ana));

        List<AdminAppointmentResponse> pendentes = service.listarAgendamentosPendentes();

        assertEquals(1, pendentes.size());
        assertEquals("Ana", pendentes.get(0).clienteNome());
        assertEquals(PASSADO, pendentes.get(0).data());
    }

    @Test
    void concluirAgendamento_passadoAtivo_concluiENotifica() {
        Appointment passado = new Appointment(PASSADO, HORA, 7L);
        when(appointments.findById(10L)).thenReturn(java.util.Optional.of(passado));

        service.concluirAgendamento(10L);

        assertEquals(AppointmentStatus.CONCLUIDO, passado.getStatus());
        verify(appointments).save(passado);
        verify(notifications).notificar(eq(7L), anyString());
    }

    @Test
    void concluirAgendamento_futuro_lancaBadRequest() {
        Appointment futuro = new Appointment(FUTURO, HORA, 7L);
        when(appointments.findById(10L)).thenReturn(java.util.Optional.of(futuro));

        assertThrows(BadRequestException.class, () -> service.concluirAgendamento(10L));

        verify(appointments, never()).save(any());
        verify(notifications, never()).notificar(any(), anyString());
    }

    @Test
    void concluirAgendamento_jaConcluido_lancaBadRequest() {
        Appointment passado = new Appointment(PASSADO, HORA, 7L);
        passado.concluir();
        when(appointments.findById(10L)).thenReturn(java.util.Optional.of(passado));

        assertThrows(BadRequestException.class, () -> service.concluirAgendamento(10L));

        verify(appointments, never()).save(any());
    }

    @Test
    void concluirAgendamento_inexistente_lancaNotFound() {
        when(appointments.findById(99L)).thenReturn(java.util.Optional.empty());

        assertThrows(NotFoundException.class, () -> service.concluirAgendamento(99L));
    }
}
