-- Troca a senha do administrador semeado na V4 (que usava um default fraco e
-- previsível). Migration nova em vez de editar a V4, para não quebrar o checksum
-- do Flyway em bancos que já a aplicaram.
--
-- Nova senha (hash BCrypt, mesmo encoder da aplicação): "Barbearia@Admin2026".
UPDATE users
   SET senha = '$2a$10$aJEtOIyVIR4yRugTzsiD8ejiC4wopFHoKonpGVPCAHZZvlJvkDeBG'
 WHERE email = 'admin@barbearia.com';
