package com.barbearia.agendamento.security;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Rate limiting simples por IP (token bucket em memória). Protege a API de
 * abuso — em especial {@code /api/auth/**} contra brute-force e spam de
 * cadastro — sem depender de infraestrutura externa (adequado ao free tier).
 *
 * <p>Dois baldes por cliente: um mais rígido para autenticação e outro geral.
 * O IP real é lido do {@code X-Forwarded-For} porque, no Render, a aplicação
 * fica atrás de um proxy — sem isso todos os clientes cairiam no mesmo balde.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    /** Teto de clientes rastreados para o mapa não crescer sem limite (free tier). */
    private static final int MAX_TRACKED_CLIENTS = 10_000;

    private final boolean enabled;
    private final int capacity;      // requisições/min por IP (geral)
    private final int authCapacity;  // requisições/min por IP em /api/auth/**

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(
            @Value("${app.rate-limit.enabled:true}") boolean enabled,
            @Value("${app.rate-limit.capacity:120}") int capacity,
            @Value("${app.rate-limit.auth-capacity:10}") int authCapacity) {
        this.enabled = enabled;
        this.capacity = capacity;
        this.authCapacity = authCapacity;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Não conta o preflight de CORS nem quando o recurso está desligado.
        if (!enabled || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean isAuth = request.getRequestURI().startsWith("/api/auth/");
        int limit = isAuth ? authCapacity : capacity;
        String key = clientIp(request) + (isAuth ? "|auth" : "|gen");

        // Salvaguarda de memória: reinicia o rastreamento se estourar o teto.
        if (buckets.size() > MAX_TRACKED_CLIENTS) {
            buckets.clear();
        }

        Bucket bucket = buckets.computeIfAbsent(key, k -> new Bucket(limit));
        if (bucket.tryConsume()) {
            filterChain.doFilter(request, response);
        } else {
            tooManyRequests(response);
        }
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // O primeiro IP da lista é o cliente original (os demais são proxies).
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Responde 429 no mesmo formato do {@code ApiError} da aplicação
     * ({@code status, error, message, timestamp}). O JSON é escrito à mão para
     * não depender do ObjectMapper aqui no filtro.
     */
    private void tooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HttpHeaders.RETRY_AFTER, "60");
        String body = "{\"status\":429,"
                + "\"error\":\"Too Many Requests\","
                + "\"message\":\"Muitas requisições em pouco tempo. Aguarde um instante e tente novamente.\","
                + "\"timestamp\":\"" + OffsetDateTime.now() + "\"}";
        response.getWriter().write(body);
    }

    /**
     * Token bucket: {@code capacity} tokens que se recarregam linearmente ao
     * longo de 1 minuto. Cada requisição consome 1 token.
     */
    private static final class Bucket {

        private final double capacity;
        private final double refillPerMillis;
        private double tokens;
        private long lastRefill;

        Bucket(int capacityPerMinute) {
            this.capacity = capacityPerMinute;
            this.refillPerMillis = capacityPerMinute / 60_000.0;
            this.tokens = capacityPerMinute;
            this.lastRefill = System.currentTimeMillis();
        }

        synchronized boolean tryConsume() {
            long now = System.currentTimeMillis();
            tokens = Math.min(capacity, tokens + (now - lastRefill) * refillPerMillis);
            lastRefill = now;
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }
    }
}
