package com.barbearia.agendamento.notification;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Destinatário (FK para users.id). */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 280)
    private String mensagem;

    @Column(nullable = false)
    private boolean lida;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Notification() {
        // exigido pelo JPA
    }

    public Notification(Long userId, String mensagem, LocalDateTime createdAt) {
        this.userId = userId;
        this.mensagem = mensagem;
        this.createdAt = createdAt;
        this.lida = false;
    }

    public void marcarComoLida() {
        this.lida = true;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getMensagem() {
        return mensagem;
    }

    public boolean isLida() {
        return lida;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
