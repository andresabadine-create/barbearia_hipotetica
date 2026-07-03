-- Fase 2 (fidelidade): cartão "a cada 10 cortes, 1 grátis".
-- O progresso (cortes concluídos) é derivado dos agendamentos passados; aqui
-- guardamos apenas quantas recompensas o cliente já resgatou, para não recontar.
CREATE TABLE loyalty_card (
    id                     BIGSERIAL PRIMARY KEY,
    user_id                BIGINT NOT NULL UNIQUE REFERENCES users (id),
    recompensas_resgatadas INT    NOT NULL DEFAULT 0
);
