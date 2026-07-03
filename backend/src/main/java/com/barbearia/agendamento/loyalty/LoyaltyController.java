package com.barbearia.agendamento.loyalty;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barbearia.agendamento.loyalty.dto.LoyaltyResponse;

@RestController
@RequestMapping("/api/loyalty")
public class LoyaltyController {

    private final LoyaltyService service;

    public LoyaltyController(LoyaltyService service) {
        this.service = service;
    }

    @GetMapping
    public LoyaltyResponse cartao(@AuthenticationPrincipal Long userId) {
        return service.getCartao(userId);
    }

    @PostMapping("/resgatar")
    public LoyaltyResponse resgatar(@AuthenticationPrincipal Long userId) {
        return service.resgatar(userId);
    }
}
