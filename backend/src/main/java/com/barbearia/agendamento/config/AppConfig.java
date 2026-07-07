package com.barbearia.agendamento.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// @EnableScheduling habilita o job de limpeza periódica do ambiente de demonstração.
@Configuration
@EnableScheduling
public class AppConfig {

    /** Relógio do sistema — injetado nos serviços para permitir testes determinísticos. */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
