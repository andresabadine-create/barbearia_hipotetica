-- Usuários da barbearia (clientes)
CREATE TABLE users (
    id    BIGSERIAL PRIMARY KEY,
    nome  VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL
);

-- Agendamentos
CREATE TABLE appointments (
    id      BIGSERIAL PRIMARY KEY,
    data    DATE        NOT NULL,
    hora    TIME        NOT NULL,
    status  VARCHAR(20) NOT NULL,
    user_id BIGINT      NOT NULL REFERENCES users (id)
);

-- Regra de negócio "não pode ter dois horários iguais": vaga única na barbearia,
-- válida apenas entre agendamentos ATIVOS (cancelados liberam a vaga).
CREATE UNIQUE INDEX ux_appointment_slot_ativo
    ON appointments (data, hora)
    WHERE status = 'AGENDADO';

-- Acelera a listagem dos agendamentos por usuário.
CREATE INDEX ix_appointment_user ON appointments (user_id);
