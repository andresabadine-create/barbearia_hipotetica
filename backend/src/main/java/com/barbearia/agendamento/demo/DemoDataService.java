package com.barbearia.agendamento.demo;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barbearia.agendamento.appointment.Appointment;
import com.barbearia.agendamento.appointment.AppointmentRepository;
import com.barbearia.agendamento.loyalty.LoyaltyCardRepository;
import com.barbearia.agendamento.notification.Notification;
import com.barbearia.agendamento.notification.NotificationRepository;
import com.barbearia.agendamento.user.Role;
import com.barbearia.agendamento.user.User;
import com.barbearia.agendamento.user.UserRepository;

/**
 * Mantém o ambiente de demonstração limpo e povoado. Periodicamente (cron
 * configurável — padrão a cada 6h) apaga tudo que os visitantes criaram e
 * re-semeia um conjunto de exemplo, para que cada recrutador encontre a
 * plataforma apresentável e sem o acúmulo de visitas anteriores.
 *
 * <p>A conta {@code ADMIN} é sempre preservada. Um aviso no topo do frontend
 * lembra o visitante de que os dados podem ser reiniciados durante a sessão.
 */
@Service
public class DemoDataService {

    private static final Logger log = LoggerFactory.getLogger(DemoDataService.class);

    static final String DEMO_EMAIL = "cliente.demo@barbearia.com";
    private static final String DEMO_NOME = "Cliente Demonstração";
    private static final String DEMO_TELEFONE = "11999990000";
    private static final String DEMO_SENHA = "Cliente@Demo2026";

    /** Cortes já concluídos do cliente demo: fecha 1 cartão (10) e sobra progresso. */
    private static final int CORTES_CONCLUIDOS = 13;

    private final UserRepository users;
    private final AppointmentRepository appointments;
    private final NotificationRepository notifications;
    private final LoyaltyCardRepository loyaltyCards;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final boolean resetEnabled;

    public DemoDataService(UserRepository users, AppointmentRepository appointments,
            NotificationRepository notifications, LoyaltyCardRepository loyaltyCards,
            PasswordEncoder passwordEncoder, Clock clock,
            @Value("${app.demo.reset.enabled:true}") boolean resetEnabled) {
        this.users = users;
        this.appointments = appointments;
        this.notifications = notifications;
        this.loyaltyCards = loyaltyCards;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
        this.resetEnabled = resetEnabled;
    }

    /** Na subida da aplicação, garante que o conjunto de demonstração exista. */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedIfMissing() {
        if (!users.existsByEmail(DEMO_EMAIL)) {
            seed();
            log.info("Dados de demonstração semeados (cliente demo ausente).");
        }
    }

    /** Limpeza periódica: apaga os dados dos visitantes e re-semeia o exemplo. */
    @Scheduled(
            cron = "${app.demo.reset.cron:0 0 */6 * * *}",
            zone = "${app.demo.reset.zone:America/Sao_Paulo}")
    @Transactional
    public void scheduledReset() {
        if (!resetEnabled) {
            return;
        }
        resetAndSeed();
        log.info("Ambiente de demonstração reiniciado (limpeza periódica).");
    }

    /** Apaga dados dos visitantes (preservando o ADMIN) e re-semeia o exemplo. */
    @Transactional
    public void resetAndSeed() {
        // Ordem segura de FK: filhos primeiro, depois os usuários visitantes.
        notifications.deleteAllInBatch();
        appointments.deleteAllInBatch();
        loyaltyCards.deleteAllInBatch();
        users.deleteByRoleNot(Role.ADMIN);
        seed();
    }

    private void seed() {
        User demo = users.save(new User(DEMO_NOME, DEMO_EMAIL,
                passwordEncoder.encode(DEMO_SENHA), DEMO_TELEFONE));
        Long uid = demo.getId();

        LocalDate hoje = LocalDate.now(clock);

        // Cortes já confirmados (CONCLUIDO) -> alimentam o cartão fidelidade.
        for (int i = 1; i <= CORTES_CONCLUIDOS; i++) {
            Appointment concluido = new Appointment(hoje.minusDays(i * 3L), LocalTime.of(10, 0), uid);
            concluido.concluir();
            appointments.save(concluido);
        }

        // Um atendimento passado ainda AGENDADO -> aparece em "Confirmar atendimentos".
        appointments.save(new Appointment(hoje.minusDays(1), LocalTime.of(15, 0), uid));

        // Um próximo agendamento (futuro) -> lista de agendamentos do cliente.
        appointments.save(new Appointment(hoje.plusDays(3), LocalTime.of(16, 30), uid));

        // Notificações in-app de exemplo.
        LocalDateTime agora = LocalDateTime.now(clock);
        notifications.save(new Notification(uid,
                "Bem-vindo(a) à Barbearia! Seu cartão fidelidade já está acumulando cortes.",
                agora.minusDays(2)));
        notifications.save(new Notification(uid,
                "Você tem um corte grátis disponível para resgatar. 🎉",
                agora.minusHours(3)));
    }
}
