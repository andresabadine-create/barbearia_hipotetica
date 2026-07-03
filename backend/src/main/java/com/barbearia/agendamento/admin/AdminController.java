package com.barbearia.agendamento.admin;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.barbearia.agendamento.admin.dto.AdminAppointmentResponse;
import com.barbearia.agendamento.admin.dto.AdminUserResponse;
import com.barbearia.agendamento.admin.dto.AvisoRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

    @GetMapping("/users")
    public List<AdminUserResponse> usuarios() {
        return service.listarUsuarios();
    }

    @PostMapping("/avisos")
    @ResponseStatus(HttpStatus.CREATED)
    public void enviarAviso(@Valid @RequestBody AvisoRequest request) {
        service.enviarAviso(request.mensagem(), request.destinatarioIds());
    }

    /** Agendamentos vencidos aguardando confirmação de conclusão. */
    @GetMapping("/appointments")
    public List<AdminAppointmentResponse> agendamentosPendentes() {
        return service.listarAgendamentosPendentes();
    }

    /** Confirma a conclusão de um agendamento (habilita a fidelidade do cliente). */
    @PostMapping("/appointments/{id}/concluir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void concluirAgendamento(@PathVariable Long id) {
        service.concluirAgendamento(id);
    }
}
