package com.barbearia.agendamento.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private final JwtService service =
            new JwtService("chave-de-teste-0123456789-0123456789-0123456789", 3_600_000L);

    @Test
    void token_preservaUserIdERole() {
        String token = service.generateToken(42L, "admin@ex.com", "ADMIN");

        JwtService.AuthenticatedUser principal = service.parse(token);

        assertEquals(42L, principal.userId());
        assertEquals("ADMIN", principal.role());
    }

    @Test
    void tokenInvalido_lancaExcecao() {
        assertThrows(Exception.class, () -> service.parse("nao-e-um-token-valido"));
    }
}
