package com.barbearia.agendamento.loyalty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Cartão fidelidade do cliente. O número de cortes concluídos é derivado dos
 * agendamentos; persistimos apenas quantas recompensas já foram resgatadas.
 */
@Entity
@Table(name = "loyalty_card")
public class LoyaltyCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "recompensas_resgatadas", nullable = false)
    private int recompensasResgatadas;

    protected LoyaltyCard() {
        // exigido pelo JPA
    }

    public LoyaltyCard(Long userId) {
        this.userId = userId;
        this.recompensasResgatadas = 0;
    }

    /** Marca mais um corte grátis como resgatado. */
    public void resgatar() {
        this.recompensasResgatadas++;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public int getRecompensasResgatadas() {
        return recompensasResgatadas;
    }
}
