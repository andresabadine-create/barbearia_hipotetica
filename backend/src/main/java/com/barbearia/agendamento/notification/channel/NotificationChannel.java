package com.barbearia.agendamento.notification.channel;

import com.barbearia.agendamento.user.User;

/**
 * Canal externo de entrega de notificações (ex.: WhatsApp).
 *
 * <p>Gancho de extensão: hoje existe apenas uma implementação que registra em
 * log ({@link LogNotificationChannel}). Para ativar o envio real, adicione uma
 * implementação (Twilio, WhatsApp Business API, ...) e marque-a como
 * {@code @Primary} — o resto do sistema não muda.
 */
public interface NotificationChannel {

    void enviar(User destinatario, String mensagem);
}
