package com.barbearia.agendamento.appointment;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.barbearia.agendamento.appointment.dto.AppointmentResponse;
import com.barbearia.agendamento.appointment.dto.CreateAppointmentRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    @GetMapping
    public List<AppointmentResponse> listar(@AuthenticationPrincipal Long userId) {
        return service.listarDoUsuario(userId).stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse criar(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateAppointmentRequest request) {
        Appointment criado = service.criar(userId, request.data(), request.hora());
        return AppointmentResponse.from(criado);
    }

    @PatchMapping("/{id}/cancel")
    public AppointmentResponse cancelar(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        return AppointmentResponse.from(service.cancelar(userId, id));
    }
}
