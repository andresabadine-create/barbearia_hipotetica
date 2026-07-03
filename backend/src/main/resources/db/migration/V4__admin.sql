-- Fase 3 (admin): papel do usuário + administrador semeado.

-- Papel de acesso. Usuários existentes e novos cadastros são USER por padrão.
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';

-- Administrador inicial para acessar o painel.
-- Credenciais padrão: admin@barbearia.com / admin12345  (TROQUE EM PRODUÇÃO).
-- A senha está como hash BCrypt (mesmo encoder da aplicação).
INSERT INTO users (nome, email, senha, telefone, role)
VALUES (
    'Administrador',
    'admin@barbearia.com',
    '$2a$10$WSwpJ65SzUcZ/BtSt7nl1eOKLzYJP6.ohCB0Db1.Qa0Wg3v0yXZ9u',
    '00000000000',
    'ADMIN'
);
