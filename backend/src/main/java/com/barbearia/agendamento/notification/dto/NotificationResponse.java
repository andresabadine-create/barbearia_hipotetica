package com.barbearia.agendamento.notification.dto;

import java.time.LocalDateTime;

import com.barbearia.agendamento.notification.Notification;

public record NotificationResponse(
        Long id,
        String mensagem,
        boolean lida,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(n.getId(), n.getMensagem(), n.isLida(), n.getCreatedAt());
    }
}
