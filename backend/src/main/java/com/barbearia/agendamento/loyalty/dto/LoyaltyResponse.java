package com.barbearia.agendamento.loyalty.dto;

/**
 * Estado do cartão fidelidade.
 *
 * @param carimbos              cortes no cartão atual (0..meta-1)
 * @param meta                  cortes necessários para ganhar um grátis
 * @param recompensasDisponiveis cortes grátis ainda não resgatados
 * @param cortesConcluidos       total de cortes concluídos pelo cliente
 */
public record LoyaltyResponse(
        int carimbos,
        int meta,
        int recompensasDisponiveis,
        int cortesConcluidos
) {
}
