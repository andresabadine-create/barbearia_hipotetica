package com.barbearia.agendamento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

// Autenticação é via JWT (sem form/basic login), então dispensamos o usuário
// em memória padrão do Spring Security e o log de senha gerada.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class AgendamentoApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgendamentoApplication.class, args);
	}

}
