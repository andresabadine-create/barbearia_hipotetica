package com.barbearia.agendamento.loyalty;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barbearia.agendamento.appointment.AppointmentRepository;
import com.barbearia.agendamento.appointment.AppointmentStatus;
import com.barbearia.agendamento.common.exception.BadRequestException;
import com.barbearia.agendamento.loyalty.dto.LoyaltyResponse;

@Service
public class LoyaltyService {

    /** Cartão fecha a cada 10 cortes concluídos, gerando 1 corte grátis. */
    static final int META_CARTAO = 10;

    private final AppointmentRepository appointments;
    private final LoyaltyCardRepository cards;

    public LoyaltyService(AppointmentRepository appointments, LoyaltyCardRepository cards) {
        this.appointments = appointments;
        this.cards = cards;
    }

    /** Estado atual do cartão do usuário (sem efeitos colaterais). */
    @Transactional(readOnly = true)
    public LoyaltyResponse getCartao(Long userId) {
        int concluidos = contarCortesConcluidos(userId);
        int resgatadas = cards.findByUserId(userId)
                .map(LoyaltyCard::getRecompensasResgatadas)
                .orElse(0);
        return montarResposta(concluidos, resgatadas);
    }

    /**
     * Resgata um corte grátis. Falha (400) se o cliente ainda não tiver
     * completado um cartão que ainda não foi resgatado.
     */
    @Transactional
    public LoyaltyResponse resgatar(Long userId) {
        int concluidos = contarCortesConcluidos(userId);
        LoyaltyCard card = cards.findByUserId(userId).orElseGet(() -> new LoyaltyCard(userId));

        int disponiveis = concluidos / META_CARTAO - card.getRecompensasResgatadas();
        if (disponiveis <= 0) {
            throw new BadRequestException("Você ainda não tem um corte grátis para resgatar.");
        }

        card.resgatar();
        cards.save(card);
        return montarResposta(concluidos, card.getRecompensasResgatadas());
    }

    /**
     * Cortes concluídos = agendamentos que o admin confirmou como realizados
     * (status {@code CONCLUIDO}). Só a confirmação do admin habilita o resgate.
     */
    private int contarCortesConcluidos(Long userId) {
        return appointments.findByUserIdAndStatus(userId, AppointmentStatus.CONCLUIDO).size();
    }

    private LoyaltyResponse montarResposta(int concluidos, int resgatadas) {
        int disponiveis = Math.max(0, concluidos / META_CARTAO - resgatadas);
        int carimbos = concluidos % META_CARTAO;
        return new LoyaltyResponse(carimbos, META_CARTAO, disponiveis, concluidos);
    }
}
