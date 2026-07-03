package com.barbearia.agendamento.admin;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Verificação temporária: o hash semeado do admin bate com a senha documentada. */
class SeedHashCheck {

    @Test
    void seedHashMatchesDocumentedPassword() {
        String hash = "$2a$10$WSwpJ65SzUcZ/BtSt7nl1eOKLzYJP6.ohCB0Db1.Qa0Wg3v0yXZ9u";
        assertTrue(new BCryptPasswordEncoder().matches("admin12345", hash),
                "O hash semeado NAO corresponde a 'admin12345'");
    }
}
