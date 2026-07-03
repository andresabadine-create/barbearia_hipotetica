package com.barbearia.agendamento.notification;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.barbearia.agendamento.notification.dto.NotificationResponse;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public List<NotificationResponse> listar(@AuthenticationPrincipal Long userId) {
        return service.listar(userId).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @GetMapping("/unread-count")
    public long naoLidas(@AuthenticationPrincipal Long userId) {
        return service.contarNaoLidas(userId);
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse marcarComoLida(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        return NotificationResponse.from(service.marcarComoLida(userId, id));
    }

    @PatchMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarTodasComoLidas(@AuthenticationPrincipal Long userId) {
        service.marcarTodasComoLidas(userId);
    }
}
