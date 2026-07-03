-- Fase 3.1: a fidelidade só conta cortes que o admin confirmar como concluídos.

-- Backfill: agendamentos ativos cujo horário já passou são considerados concluídos,
-- preservando o progresso de fidelidade acumulado até aqui. A partir de agora, novos
-- agendamentos passam a exigir a confirmação do admin no painel para contar no cartão.
UPDATE appointments
   SET status = 'CONCLUIDO'
 WHERE status = 'AGENDADO'
   AND (data + hora) < LOCALTIMESTAMP;

-- Telefone obrigatório também no banco (a aplicação já exige no cadastro). Cadastros
-- antigos sem telefone recebem um placeholder para permitir a constraint NOT NULL.
UPDATE users SET telefone = '' WHERE telefone IS NULL;
ALTER TABLE users ALTER COLUMN telefone SET NOT NULL;
