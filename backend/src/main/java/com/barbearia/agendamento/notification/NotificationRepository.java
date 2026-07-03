package com.barbearia.agendamento.notification;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** Notificações do usuário, mais recentes primeiro. */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    /** Não lidas do usuário (para marcar todas de uma vez). */
    List<Notification> findByUserIdAndLidaFalse(Long userId);

    /** Quantidade de não lidas (contador do sino). */
    long countByUserIdAndLidaFalse(Long userId);

    /** Uma notificação garantindo que pertence ao usuário. */
    Optional<Notification> findByIdAndUserId(Long id, Long userId);
}
