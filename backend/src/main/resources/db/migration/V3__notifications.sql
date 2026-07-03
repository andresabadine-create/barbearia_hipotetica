-- Fase 2 (notificações): telefone do usuário + notificações in-app.

-- Telefone para envio via WhatsApp (fica nullable para não quebrar cadastros
-- antigos; a obrigatoriedade é garantida na camada de aplicação, no cadastro).
ALTER TABLE users ADD COLUMN telefone VARCHAR(30);

-- Notificações in-app do usuário.
CREATE TABLE notification (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users (id),
    mensagem   VARCHAR(280) NOT NULL,
    lida       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL
);

-- Acelera a listagem por usuário e a contagem de não lidas (sino).
CREATE INDEX ix_notification_user ON notification (user_id, lida);
