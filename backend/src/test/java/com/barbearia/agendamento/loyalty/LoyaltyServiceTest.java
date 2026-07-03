package com.barbearia.agendamento.loyalty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.barbearia.agendamento.appointment.Appointment;
import com.barbearia.agendamento.appointment.AppointmentRepository;
import com.barbearia.agendamento.appointment.AppointmentStatus;
import com.barbearia.agendamento.common.exception.BadRequestException;
import com.barbearia.agendamento.loyalty.dto.LoyaltyResponse;

class LoyaltyServiceTest {

    private static final Long USER_ID = 1L;
    private static final LocalDate DIA = LocalDate.of(2026, 6, 29);
    private static final LocalTime HORA = LocalTime.of(10, 0);

    private AppointmentRepository appointments;
    private LoyaltyCardRepository cards;
    private LoyaltyService service;

    @BeforeEach
    void setUp() {
        appointments = mock(AppointmentRepository.class);
        cards = mock(LoyaltyCardRepository.class);
        service = new LoyaltyService(appointments, cards);
    }

    @Test
    void getCartao_semCortes_zerado() {
        stubConcluidos(List.of());
        when(cards.findByUserId(USER_ID)).thenReturn(Optional.empty());

        LoyaltyResponse resp = service.getCartao(USER_ID);

        assertEquals(0, resp.carimbos());
        assertEquals(10, resp.meta());
        assertEquals(0, resp.recompensasDisponiveis());
        assertEquals(0, resp.cortesConcluidos());
    }

    @Test
    void getCartao_contaSomenteConcluidos() {
        // Apenas os confirmados pelo admin (status CONCLUIDO) contam; agendados/cancelados não.
        stubConcluidos(cortes(3));
        when(cards.findByUserId(USER_ID)).thenReturn(Optional.empty());

        LoyaltyResponse resp = service.getCartao(USER_ID);

        assertEquals(3, resp.cortesConcluidos());
        assertEquals(3, resp.carimbos());
        assertEquals(0, resp.recompensasDisponiveis());
    }

    @Test
    void getCartao_dezCortes_geraUmaRecompensa() {
        stubConcluidos(cortes(10));
        when(cards.findByUserId(USER_ID)).thenReturn(Optional.empty());

        LoyaltyResponse resp = service.getCartao(USER_ID);

        assertEquals(0, resp.carimbos());
        assertEquals(1, resp.recompensasDisponiveis());
        assertEquals(10, resp.cortesConcluidos());
    }

    @Test
    void getCartao_descontaRecompensasJaResgatadas() {
        stubConcluidos(cortes(23)); // 2 cartões completos + 3 no atual
        when(cards.findByUserId(USER_ID)).thenReturn(Optional.of(cardComResgatadas(1)));

        LoyaltyResponse resp = service.getCartao(USER_ID);

        assertEquals(3, resp.carimbos());
        assertEquals(1, resp.recompensasDisponiveis()); // 2 ganhas - 1 resgatada
    }

    @Test
    void resgatar_semRecompensa_lancaBadRequest() {
        stubConcluidos(cortes(5));
        when(cards.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> service.resgatar(USER_ID));

        verify(cards, never()).save(any());
    }

    @Test
    void resgatar_comRecompensa_incrementaResgatadas() {
        stubConcluidos(cortes(10));
        when(cards.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(cards.save(any(LoyaltyCard.class))).thenAnswer(inv -> inv.getArgument(0));

        LoyaltyResponse resp = service.resgatar(USER_ID);

        assertEquals(0, resp.recompensasDisponiveis()); // consumiu a única disponível
        verify(cards).save(any(LoyaltyCard.class));
    }

    // ---- helpers ----

    private void stubConcluidos(List<Appointment> lista) {
        when(appointments.findByUserIdAndStatus(eq(USER_ID), eq(AppointmentStatus.CONCLUIDO)))
                .thenReturn(lista);
    }

    private List<Appointment> cortes(int n) {
        return IntStream.range(0, n)
                .mapToObj(i -> new Appointment(DIA, HORA, USER_ID))
                .toList();
    }

    private LoyaltyCard cardComResgatadas(int quantidade) {
        LoyaltyCard card = new LoyaltyCard(USER_ID);
        for (int i = 0; i < quantidade; i++) {
            card.resgatar();
        }
        return card;
    }
}
