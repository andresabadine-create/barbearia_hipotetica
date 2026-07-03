package com.barbearia.agendamento.notification.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.barbearia.agendamento.user.User;

/**
 * Implementação padrão do canal externo: por enquanto apenas registra em log o
 * envio que aconteceria. É o "gancho" pronto para o provedor de WhatsApp — basta
 * criar uma nova implementação real e marcá-la como {@code @Primary}.
 */
@Component
public class LogNotificationChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(LogNotificationChannel.class);

    @Override
    public void enviar(User destinatario, String mensagem) {
        log.info("[WhatsApp pendente de provedor] Para {} ({}): {}",
                destinatario.getNome(), destinatario.getTelefone(), mensagem);
    }
}
