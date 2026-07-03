package com.barbearia.agendamento.notification;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.barbearia.agendamento.common.exception.NotFoundException;
import com.barbearia.agendamento.notification.channel.NotificationChannel;
import com.barbearia.agendamento.user.User;
import com.barbearia.agendamento.user.UserRepository;

class NotificationServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-06-30T12:00:00Z"), ZoneOffset.UTC);

    private static final Long USER_ID = 1L;

    private NotificationRepository notifications;
    private UserRepository users;
    private NotificationChannel channel;
    private NotificationService service;

    @BeforeEach
    void setUp() {
        notifications = mock(NotificationRepository.class);
        users = mock(UserRepository.class);
        channel = mock(NotificationChannel.class);
        service = new NotificationService(notifications, users, channel, clock);
    }

    @Test
    void notificar_salvaInAppEDisparaCanal() {
        User user = new User("Ana", "ana@ex.com", "hash", "11999998888");
        when(users.findById(USER_ID)).thenReturn(Optional.of(user));

        service.notificar(USER_ID, "Agendamento confirmado.");

        verify(notifications).save(any(Notification.class));
        verify(channel).enviar(eq(user), eq("Agendamento confirmado."));
    }

    @Test
    void notificar_quandoCanalFalha_naoPropagaErro() {
        User user = new User("Ana", "ana@ex.com", "hash", "11999998888");
        when(users.findById(USER_ID)).thenReturn(Optional.of(user));
        doThrow(new RuntimeException("provedor fora do ar"))
                .when(channel).enviar(any(), anyString());

        assertDoesNotThrow(() -> service.notificar(USER_ID, "oi"));

        // A notificação in-app é persistida mesmo com o canal externo falhando.
        verify(notifications).save(any(Notification.class));
    }

    @Test
    void notificar_semUsuario_naoChamaCanal() {
        when(users.findById(USER_ID)).thenReturn(Optional.empty());

        service.notificar(USER_ID, "oi");

        verify(notifications).save(any(Notification.class));
        verify(channel, never()).enviar(any(), anyString());
    }

    @Test
    void contarNaoLidas_delegaAoRepositorio() {
        when(notifications.countByUserIdAndLidaFalse(USER_ID)).thenReturn(4L);

        assertEquals(4L, service.contarNaoLidas(USER_ID));
    }

    @Test
    void marcarComoLida_inexistente_lancaNotFound() {
        when(notifications.findByIdAndUserId(9L, USER_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.marcarComoLida(USER_ID, 9L));
    }

    @Test
    void marcarComoLida_marcaEPersiste() {
        Notification n = new Notification(USER_ID, "oi", LocalDateTime.now(clock));
        when(notifications.findByIdAndUserId(3L, USER_ID)).thenReturn(Optional.of(n));
        when(notifications.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        Notification resultado = service.marcarComoLida(USER_ID, 3L);

        assertTrue(resultado.isLida());
        verify(notifications).save(n);
    }

    @Test
    void marcarTodasComoLidas_marcaTodasAsNaoLidas() {
        Notification a = new Notification(USER_ID, "a", LocalDateTime.now(clock));
        Notification b = new Notification(USER_ID, "b", LocalDateTime.now(clock));
        when(notifications.findByUserIdAndLidaFalse(USER_ID)).thenReturn(List.of(a, b));

        service.marcarTodasComoLidas(USER_ID);

        assertTrue(a.isLida());
        assertTrue(b.isLida());
        verify(notifications).saveAll(anyList());
    }
}
