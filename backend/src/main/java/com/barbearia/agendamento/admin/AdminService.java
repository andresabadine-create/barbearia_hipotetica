package com.barbearia.agendamento.admin;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barbearia.agendamento.admin.dto.AdminAppointmentResponse;
import com.barbearia.agendamento.admin.dto.AdminUserResponse;
import com.barbearia.agendamento.appointment.Appointment;
import com.barbearia.agendamento.appointment.AppointmentRepository;
import com.barbearia.agendamento.appointment.AppointmentStatus;
import com.barbearia.agendamento.common.exception.BadRequestException;
import com.barbearia.agendamento.common.exception.NotFoundException;
import com.barbearia.agendamento.notification.NotificationService;
import com.barbearia.agendamento.user.User;
import com.barbearia.agendamento.user.UserRepository;

@Service
public class AdminService {

    private static final DateTimeFormatter DATA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final UserRepository users;
    private final NotificationService notifications;
    private final AppointmentRepository appointments;
    private final Clock clock;

    public AdminService(
            UserRepository users,
            NotificationService notifications,
            AppointmentRepository appointments,
            Clock clock) {
        this.users = users;
        this.notifications = notifications;
        this.appointments = appointments;
        this.clock = clock;
    }

    /** Lista todos os cadastros (gestão de usuários). */
    @Transactional(readOnly = true)
    public List<AdminUserResponse> listarUsuarios() {
        return users.findAll().stream()
                .map(AdminUserResponse::from)
                .toList();
    }

    /**
     * Envia um aviso (como notificação) a destinatários específicos. Falha se a
     * lista estiver vazia ou se algum id não corresponder a um usuário existente.
     *
     * @return quantidade de destinatários notificados
     */
    @Transactional
    public int enviarAviso(String mensagem, List<Long> destinatarioIds) {
        if (destinatarioIds == null || destinatarioIds.isEmpty()) {
            throw new BadRequestException("Selecione ao menos um destinatário.");
        }
        List<Long> distintos = destinatarioIds.stream().distinct().toList();
        List<User> destinatarios = users.findAllById(distintos);
        if (destinatarios.size() != distintos.size()) {
            throw new BadRequestException("Um ou mais destinatários não foram encontrados.");
        }
        destinatarios.forEach(user -> notifications.notificar(user.getId(), "📢 " + mensagem));
        return destinatarios.size();
    }

    /**
     * Agendamentos ativos cujo horário já passou e que aguardam a confirmação de
     * conclusão pelo admin (fila do painel), mais recentes primeiro.
     */
    @Transactional(readOnly = true)
    public List<AdminAppointmentResponse> listarAgendamentosPendentes() {
        LocalDateTime agora = LocalDateTime.now(clock);
        List<Appointment> pendentes = appointments
                .findByStatusOrderByDataDescHoraDesc(AppointmentStatus.AGENDADO).stream()
                .filter(a -> !LocalDateTime.of(a.getData(), a.getHora()).isAfter(agora))
                .toList();

        Map<Long, String> nomePorUsuario = users
                .findAllById(pendentes.stream().map(Appointment::getUserId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(User::getId, User::getNome, (a, b) -> a));

        return pendentes.stream()
                .map(a -> AdminAppointmentResponse.from(
                        a, nomePorUsuario.getOrDefault(a.getUserId(), "Cliente")))
                .toList();
    }

    /**
     * Confirma que um agendamento foi de fato concluído. Só é possível concluir um
     * agendamento ativo cujo horário já passou; ao concluir, o corte passa a contar
     * no cartão fidelidade e o cliente é notificado.
     */
    @Transactional
    public void concluirAgendamento(Long appointmentId) {
        Appointment agendamento = appointments.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Agendamento não encontrado."));

        if (agendamento.getStatus() == AppointmentStatus.CONCLUIDO) {
            throw new BadRequestException("Este agendamento já foi concluído.");
        }
        if (!agendamento.isAgendado()) {
            throw new BadRequestException("Só é possível concluir um agendamento ativo.");
        }
        if (LocalDateTime.of(agendamento.getData(), agendamento.getHora())
                .isAfter(LocalDateTime.now(clock))) {
            throw new BadRequestException("Não é possível concluir um agendamento que ainda não aconteceu.");
        }

        agendamento.concluir();
        appointments.save(agendamento);
        notifications.notificar(agendamento.getUserId(),
                "✅ Seu corte de %s às %s foi confirmado e já conta no seu cartão fidelidade."
                        .formatted(agendamento.getData().format(DATA_FMT),
                                agendamento.getHora().format(HORA_FMT)));
    }
}
