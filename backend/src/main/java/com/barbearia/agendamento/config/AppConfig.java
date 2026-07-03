package com.barbearia.agendamento.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    /** Relógio do sistema — injetado nos serviços para permitir testes determinísticos. */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
