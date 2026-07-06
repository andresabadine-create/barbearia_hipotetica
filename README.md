# 💈 Barbearia Hipotética — Sistema de Agendamentos

Aplicação web **full-stack** onde o cliente se cadastra, faz login e gerencia
seus agendamentos em uma barbearia. Inclui **plano de fidelidade**,
**notificações** in-app e um **painel administrativo**.

![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F?logo=springboot&logoColor=white)
![Angular](https://img.shields.io/badge/Angular-22-DD0031?logo=angular&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white)
![JWT](https://img.shields.io/badge/Auth-JWT-000000?logo=jsonwebtokens&logoColor=white)

> 🎯 **Projeto de portfólio.** Construí este sistema para praticar, de ponta a
> ponta, os fundamentos de uma aplicação full-stack real: autenticação, regras de
> negócio, banco relacional com migrações, testes e deploy em nuvem. É um MVP
> deliberadamente enxuto no escopo, mas cuidadoso na engenharia.

---

## 🔗 Demo ao vivo

**👉 [barbearia-hipotetica.vercel.app](https://barbearia-hipotetica.vercel.app)**

Você pode **criar sua própria conta** ou entrar com o administrador de demonstração:

| Perfil | E-mail | Senha |
|---|---|---|
| 👑 Admin | `admin@barbearia.com` | `admin12345` |

> ⏳ **Primeiro acesso pode levar ~30–50s.** O backend roda em _free tier_ e
> hiberna quando fica ocioso; a primeira requisição o "acorda". As seguintes são
> instantâneas.

---

## ✨ Funcionalidades

**Cliente**
- 🔐 Cadastro e login com autenticação **JWT**
- 📅 Criar, listar e cancelar os próprios agendamentos
- 🎟️ **Cartão fidelidade** — a cada 10 cortes concluídos, 1 corte grátis
- 🔔 **Notificações** in-app (sino com contador de não lidas)

**Administrador**
- 👥 Gestão de todos os cadastros
- 📢 Envio de avisos para clientes selecionados
- ✅ Confirmação de atendimentos (o que habilita a fidelidade)

---

## 🧠 Regras de negócio

O foco do projeto está no **domínio**, não só na tela. As regras são garantidas
no backend:

1. **Não se agenda no passado.**
2. **Vaga única por horário** — dois agendamentos ativos não podem colidir; um
   cancelamento libera a vaga.
3. **Isolamento por usuário** — cada cliente só enxerga e gerencia o que é seu.
4. **Cancelamento _soft_** — o registro vira `CANCELADO` (preserva o histórico).
5. **Fidelidade só conta corte confirmado** — não basta o horário passar; o
   admin confirma o atendimento (`CONCLUIDO`) para o corte valer no cartão.

---

## 🏗️ Arquitetura

Monorepo com backend, frontend e infraestrutura de banco separados:

```
barbearia_hipotetica/
├── backend/            # API REST (Spring Boot) — regras de negócio + segurança
├── frontend/           # SPA (Angular) — interface do cliente e do admin
├── docker-compose.yml  # PostgreSQL local
├── Dockerfile          # (em backend/) imagem de deploy do backend
└── render.yaml         # Blueprint de deploy do backend
```

**Deploy** (tudo em _free tier_):

```
Navegador → Angular (Vercel) ──/api/*──▶ Spring Boot (Render, Docker) ──▶ PostgreSQL (Neon)
```

O frontend na Vercel faz _proxy_ de `/api/*` para o backend, então o navegador vê
tudo na **mesma origem**. Passo a passo completo em **[DEPLOY.md](DEPLOY.md)**.

---

## 🛠️ Stack

| Camada | Tecnologias |
|---|---|
| **Backend** | Java 21 (LTS), Spring Boot 4.1, Spring Security (JWT), Spring Data JPA |
| **Banco** | PostgreSQL 16, migrações versionadas com **Flyway** |
| **Frontend** | Angular 22 (componentes _standalone_ + **signals**), RxJS |
| **Infra/Deploy** | Docker, Vercel (frontend), Render (backend), Neon (banco) |
| **Testes** | JUnit 5, Mockito, Spring Boot Test (banco H2 em memória) |

---

## 💡 Destaques técnicos

Decisões que pratiquei conscientemente ao construir o projeto:

- **Autenticação _stateless_ com JWT** — sem sessão em servidor; o papel do usuário
  (`USER`/`ADMIN`) viaja como _claim_ no token e controla o acesso às rotas.
- **Autorização por papel** — rotas `/api/admin/**` restritas via
  `hasRole("ADMIN")` no Spring Security.
- **Migrações com Flyway** — o schema é versionado (`V1`…`V5`) e criado
  automaticamente no primeiro boot; o banco nunca é montado "na mão".
- **Arquitetura em camadas** — Controller → Service → Repository, com regras de
  negócio isoladas na camada de serviço e cobertas por testes unitários.
- **Progresso derivado, não duplicado** — a fidelidade é calculada a partir dos
  próprios agendamentos; só o número de recompensas resgatadas é persistido
  (evita estado redundante e inconsistências).
- **Abstração de canal de notificação** — o envio é uma interface
  (`NotificationChannel`); hoje só registra em log, mas trocar por WhatsApp/Twilio
  é adicionar uma implementação, sem tocar no resto (aberto para extensão).
- **Frontend moderno** — Angular _standalone_ com **signals** para estado reativo,
  _guards_ de rota, e um _HTTP interceptor_ que injeta o token e trata sessão
  expirada (401 → logout).
- **Configuração por ambiente** — portas, banco, segredo do JWT e origens de CORS
  vêm de variáveis de ambiente; nenhum segredo no código.
- **Pronto para conteinerização** — `Dockerfile` _multi-stage_ (build com Maven,
  imagem final só com JRE).

---

## 📡 API REST

| Método | Rota | Auth | Descrição |
|---|---|---|---|
| POST | `/api/auth/register` | pública | Cadastro (retorna token) |
| POST | `/api/auth/login` | pública | Login (retorna token) |
| GET | `/api/appointments` | JWT | Lista os agendamentos do usuário |
| POST | `/api/appointments` | JWT | Cria agendamento |
| PATCH | `/api/appointments/{id}/cancel` | JWT | Cancela (soft) um agendamento |
| GET | `/api/loyalty` | JWT | Cartão fidelidade (progresso e cortes grátis) |
| POST | `/api/loyalty/resgatar` | JWT | Resgata um corte grátis disponível |
| GET | `/api/notifications` | JWT | Lista as notificações do usuário |
| GET | `/api/notifications/unread-count` | JWT | Quantidade de não lidas |
| PATCH | `/api/notifications/{id}/read` | JWT | Marca uma como lida |
| PATCH | `/api/notifications/read-all` | JWT | Marca todas como lidas |
| GET | `/api/admin/users` | ADMIN | Lista todos os cadastros |
| POST | `/api/admin/avisos` | ADMIN | Envia um aviso aos destinatários |
| GET | `/api/admin/appointments` | ADMIN | Agendamentos aguardando confirmação |
| POST | `/api/admin/appointments/{id}/concluir` | ADMIN | Confirma um atendimento |

Erros seguem um formato consistente: `{ status, error, message, timestamp }`.

---

## ▶️ Como rodar localmente

**Pré-requisitos:** Java 21, Node.js 20+, Docker.

```bash
# 1. Banco de dados (PostgreSQL em localhost:5433)
docker compose up -d

# 2. Backend (API em http://localhost:8081)
cd backend
./mvnw spring-boot:run

# 3. Frontend (SPA em http://localhost:4200)
cd frontend
npm install   # apenas na primeira vez
npm start
```

Abra **http://localhost:4200**. O Flyway cria as tabelas no primeiro boot e semeia
o admin. O dev server usa um _proxy_ que encaminha `/api` para o backend (sem CORS).

> As portas (8081 / 5433 / 4200) foram escolhidas para não conflitar com serviços
> comuns (8080 / 5432) e são todas configuráveis por variável de ambiente.

### 🧪 Testes

Regras de negócio cobertas por testes unitários (rodam em H2, sem Docker):

```bash
cd backend
./mvnw test
```

---

## 🗺️ Evolução do projeto

Construído em fases, cada uma agregando uma capacidade de negócio completa:

- ✅ **MVP** — cadastro, login (JWT) e o ciclo de agendamentos.
- ✅ **Fase 1 — Fidelidade** — cartão "a cada 10 cortes, 1 grátis", com progresso
  derivado dos agendamentos concluídos.
- ✅ **Fase 2 — Notificações** — eventos de agendamento geram notificação in-app;
  canal de envio abstraído para futura integração com WhatsApp.
- ✅ **Fase 3 — Painel administrativo** — papel `ADMIN`, gestão de cadastros,
  avisos e confirmação de atendimentos.

---

## 👤 Autor

**André Augusto Sabadine Domingues** — desenvolvedor em formação, focado em
back-end Java/Spring e full-stack.

- 💼 LinkedIn: _[adicione seu link]_
- 🐙 GitHub: _[adicione seu link]_

> Feedback é muito bem-vindo — este projeto é parte do meu aprendizado contínuo.
