package com.barbearia.agendamento.notification;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barbearia.agendamento.common.exception.NotFoundException;
import com.barbearia.agendamento.notification.channel.NotificationChannel;
import com.barbearia.agendamento.user.UserRepository;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notifications;
    private final UserRepository users;
    private final NotificationChannel channel;
    private final Clock clock;

    public NotificationService(
            NotificationRepository notifications,
            UserRepository users,
            NotificationChannel channel,
            Clock clock) {
        this.notifications = notifications;
        this.users = users;
        this.channel = channel;
        this.clock = clock;
    }

    /**
     * Cria uma notificação in-app e dispara o canal externo (WhatsApp). A falha
     * do canal externo NÃO derruba a operação de origem nem a notificação in-app.
     */
    @Transactional
    public void notificar(Long userId, String mensagem) {
        notifications.save(new Notification(userId, mensagem, LocalDateTime.now(clock)));
        users.findById(userId).ifPresent(destinatario -> {
            try {
                channel.enviar(destinatario, mensagem);
            } catch (Exception ex) {
                log.warn("Falha ao enviar notificação externa para o usuário {}", userId, ex);
            }
        });
    }

    @Transactional(readOnly = true)
    public List<Notification> listar(Long userId) {
        return notifications.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public long contarNaoLidas(Long userId) {
        return notifications.countByUserIdAndLidaFalse(userId);
    }

    @Transactional
    public Notification marcarComoLida(Long userId, Long id) {
        Notification notificacao = notifications.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Notificação não encontrada."));
        notificacao.marcarComoLida();
        return notifications.save(notificacao);
    }

    @Transactional
    public void marcarTodasComoLidas(Long userId) {
        List<Notification> naoLidas = notifications.findByUserIdAndLidaFalse(userId);
        naoLidas.forEach(Notification::marcarComoLida);
        notifications.saveAll(naoLidas);
    }
}
