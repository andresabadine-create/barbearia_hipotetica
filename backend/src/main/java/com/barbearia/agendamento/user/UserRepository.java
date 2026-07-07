package com.barbearia.agendamento.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /** Remove todos os usuários que não são do papel informado (ex.: preservar o ADMIN na limpeza da demo). */
    long deleteByRoleNot(Role role);
}
