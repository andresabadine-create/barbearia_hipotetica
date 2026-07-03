# Barbearia Hipotética — Sistema de Agendamentos (MVP)

Sistema web para uma barbearia hipotética onde o **cliente** se cadastra,
autentica e gerencia seus agendamentos. Projeto de MVP com backend em
**Spring Boot** + **PostgreSQL** e frontend em **Angular**.

## Requisitos funcionais (MVP)

- Cadastrar usuário
- Autenticar (login) — JWT
- Criar agendamento
- Listar agendamentos (apenas os próprios)
- Cancelar agendamento

## Regras de negócio

1. **Não é possível agendar no passado.**
2. **Não pode haver dois agendamentos ativos no mesmo horário** (vaga única na
   barbearia). Um agendamento cancelado libera a vaga.
3. **Cada usuário só enxerga e gerencia os próprios agendamentos.**

O cancelamento é *soft*: o registro passa a `CANCELADO` (preserva histórico).

---

## Stack e arquitetura

| Camada | Tecnologia |
|---|---|
| Backend | Java 21 (LTS), Spring Boot 4.1, Spring Security (JWT), Spring Data JPA |
| Banco | PostgreSQL 16 (via Docker), migrações com Flyway |
| Frontend | Angular 22 (standalone + signals) |

```
barbearia_hipotetica/
├── docker-compose.yml      # PostgreSQL
├── backend/                # API REST (Spring Boot)
└── frontend/               # SPA (Angular)
```

---

## Pré-requisitos

- **Java 21** (JDK). O build usa o Maven Wrapper (`mvnw`), não é preciso instalar o Maven.
- **Node.js 20+** e **npm** (para o frontend).
- **Docker** (para subir o PostgreSQL).

> **Portas usadas:** backend **8081**, PostgreSQL **5433** (host), frontend **4200**.
> Essas portas foram escolhidas para evitar conflito com serviços que já podem
> ocupar 8080/5432 na máquina (ex.: EnterpriseDB). Todas são configuráveis por
> variável de ambiente (`SERVER_PORT`, `DB_URL`, etc.).

---

## Como rodar

### 1. Banco de dados (PostgreSQL)

```bash
docker compose up -d
```

Sobe o Postgres em `localhost:5433` (db/usuário/senha: `barbearia`).

### 2. Backend (API em http://localhost:8081)

```bash
cd backend
./mvnw spring-boot:run
```

Na primeira execução o Flyway cria as tabelas (`users`, `appointments`).

### 3. Frontend (SPA em http://localhost:4200)

```bash
cd frontend
npm install     # apenas na primeira vez
npm start
```

Abra **http://localhost:4200**. O dev server usa um proxy (`proxy.conf.json`)
que encaminha `/api` para o backend (8081), evitando problemas de CORS.

---

## Endpoints da API

| Método | Rota | Auth | Descrição |
|---|---|---|---|
| POST | `/api/auth/register` | pública | Cadastro (retorna token) |
| POST | `/api/auth/login` | pública | Login (retorna token) |
| GET | `/api/appointments` | JWT | Lista os agendamentos do usuário |
| POST | `/api/appointments` | JWT | Cria agendamento |
| PATCH | `/api/appointments/{id}/cancel` | JWT | Cancela (soft) um agendamento |
| GET | `/api/loyalty` | JWT | Cartão fidelidade do usuário (progresso e cortes grátis) |
| POST | `/api/loyalty/resgatar` | JWT | Resgata um corte grátis disponível |
| GET | `/api/notifications` | JWT | Lista as notificações do usuário |
| GET | `/api/notifications/unread-count` | JWT | Quantidade de notificações não lidas |
| PATCH | `/api/notifications/{id}/read` | JWT | Marca uma notificação como lida |
| PATCH | `/api/notifications/read-all` | JWT | Marca todas as notificações como lidas |

Respostas de erro seguem o formato `{ status, error, message, timestamp }`.

### Exemplo rápido (curl)

```bash
API=http://localhost:8081

# Cadastro
curl -X POST $API/api/auth/register -H 'Content-Type: application/json' \
  -d '{"nome":"Ana","email":"ana@ex.com","senha":"senha12345","telefone":"11999998888"}'

# Login (captura o token)
TOKEN=$(curl -s -X POST $API/api/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"ana@ex.com","senha":"senha12345"}' | sed -E 's/.*"token":"([^"]+)".*/\1/')

# Criar agendamento
curl -X POST $API/api/appointments -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"data":"2026-07-15","hora":"14:00"}'

# Listar
curl $API/api/appointments -H "Authorization: Bearer $TOKEN"
```

---

## Testes

Testes unitários das regras de negócio (rodam em H2, sem precisar de Docker):

```bash
cd backend
./mvnw test
```

---

## Roadmap das fases futuras

- ✅ **Fase 1 — Plano de fidelidade** (cartão "a cada 10 cortes, 1 grátis").
- ✅ **Fase 2 — Notificações** in-app (sino + lista) e telefone no cadastro para
  envio via WhatsApp.
- ⬜ **Fase 3 — Painel administrativo** (papel `ADMIN` via seed, gestão de
  cadastros e avisos).

### Fidelidade (Fase 1)

Cada corte concluído (agendamento cujo horário já passou e não foi cancelado)
avança o cartão do cliente. Ao completar **10 cortes**, ele ganha **1 corte
grátis**, resgatável na tela "Fidelidade". O progresso é derivado dos próprios
agendamentos; só o número de recompensas já resgatadas é persistido.

### Notificações (Fase 2)

Eventos de agendamento (confirmação e cancelamento) geram uma **notificação
in-app**, exibida no sino do topo (com contador de não lidas) e na tela
"Notificações". O cadastro passou a exigir **telefone** para, no futuro, enviar
a mesma mensagem via WhatsApp: o envio externo é abstraído por
`NotificationChannel`, hoje implementado por `LogNotificationChannel` (só
registra em log). Para ativar o WhatsApp de verdade, basta adicionar uma
implementação (Twilio / WhatsApp Business API) e marcá-la como `@Primary`.
