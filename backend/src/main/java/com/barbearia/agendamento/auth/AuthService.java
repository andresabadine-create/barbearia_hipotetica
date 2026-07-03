package com.barbearia.agendamento.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barbearia.agendamento.auth.dto.AuthResponse;
import com.barbearia.agendamento.auth.dto.LoginRequest;
import com.barbearia.agendamento.auth.dto.RegisterRequest;
import com.barbearia.agendamento.common.exception.ConflictException;
import com.barbearia.agendamento.common.exception.UnauthorizedException;
import com.barbearia.agendamento.security.JwtService;
import com.barbearia.agendamento.user.User;
import com.barbearia.agendamento.user.UserRepository;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (users.existsByEmail(request.email())) {
            throw new ConflictException("Já existe uma conta com este e-mail.");
        }
        User user = new User(
                request.nome(),
                request.email(),
                passwordEncoder.encode(request.senha()),
                request.telefone());
        users.save(user);
        return buildResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = users.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("E-mail ou senha inválidos."));
        if (!passwordEncoder.matches(request.senha(), user.getSenha())) {
            throw new UnauthorizedException("E-mail ou senha inválidos.");
        }
        return buildResponse(user);
    }

    private AuthResponse buildResponse(User user) {
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getId(), user.getNome(), user.getEmail(), user.getRole());
    }
}
