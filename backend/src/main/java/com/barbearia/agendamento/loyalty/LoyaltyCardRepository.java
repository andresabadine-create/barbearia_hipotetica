package com.barbearia.agendamento.loyalty;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LoyaltyCardRepository extends JpaRepository<LoyaltyCard, Long> {

    Optional<LoyaltyCard> findByUserId(Long userId);
}
