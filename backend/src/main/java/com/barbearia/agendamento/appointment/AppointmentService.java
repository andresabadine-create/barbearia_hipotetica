package com.barbearia.agendamento.appointment;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barbearia.agendamento.common.exception.BadRequestException;
import com.barbearia.agendamento.common.exception.ConflictException;
import com.barbearia.agendamento.common.exception.NotFoundException;
import com.barbearia.agendamento.notification.NotificationService;

@Service
public class AppointmentService {

    private static final DateTimeFormatter DATA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final AppointmentRepository repository;
    private final NotificationService notifications;
    private final Clock clock;

    public AppointmentService(
            AppointmentRepository repository,
            NotificationService notifications,
            Clock clock) {
        this.repository = repository;
        this.notifications = notifications;
        this.clock = clock;
    }

    /**
     * Cria um agendamento aplicando as regras de negócio:
     * (1) não pode ser no passado; (2) a vaga (data+hora) não pode estar ocupada.
     */
    @Transactional
    public Appointment criar(Long userId, LocalDate data, LocalTime hora) {
        LocalDateTime quando = LocalDateTime.of(data, hora);
        if (quando.isBefore(LocalDateTime.now(clock))) {
            throw new BadRequestException("Não é possível agendar em um horário no passado.");
        }
        if (repository.existsByDataAndHoraAndStatus(data, hora, AppointmentStatus.AGENDADO)) {
            throw new ConflictException("Este horário já está reservado.");
        }
        Appointment salvo = repository.save(new Appointment(data, hora, userId));
        notifications.notificar(userId, "Agendamento confirmado para %s às %s."
                .formatted(data.format(DATA_FMT), hora.format(HORA_FMT)));
        return salvo;
    }

    /** Regra: o usuário só enxerga os próprios agendamentos. */
    @Transactional(readOnly = true)
    public List<Appointment> listarDoUsuario(Long userId) {
        return repository.findByUserIdOrderByDataAscHoraAsc(userId);
    }

    /**
     * Cancela (soft) um agendamento do próprio usuário. Se não pertencer a ele
     * (ou não existir), devolve 404 — sem vazar a existência de dados de terceiros.
     */
    @Transactional
    public Appointment cancelar(Long userId, Long appointmentId) {
        Appointment agendamento = repository.findByIdAndUserId(appointmentId, userId)
                .orElseThrow(() -> new NotFoundException("Agendamento não encontrado."));
        agendamento.cancelar();
        Appointment salvo = repository.save(agendamento);
        notifications.notificar(userId, "Agendamento de %s às %s foi cancelado."
                .formatted(agendamento.getData().format(DATA_FMT),
                        agendamento.getHora().format(HORA_FMT)));
        return salvo;
    }
}
