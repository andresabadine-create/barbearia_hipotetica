# Deploy (demo) — Vercel + Render + Neon

Guia para colocar o projeto no ar **de graça**, para testes e exposição a
recrutadores. A arquitetura fica assim:

```
   Navegador
      │
      ▼
  Angular (Vercel)  ──/api/*──▶  Spring Boot (Render, Docker)  ──▶  PostgreSQL (Neon)
```

O frontend na Vercel usa um **rewrite** (`frontend/vercel.json`) que encaminha
`/api/*` para o backend no Render. Como o proxy é feito no servidor da Vercel, o
navegador enxerga tudo na **mesma origem** — não há CORS no caminho principal.

> **⚠️ Custo e cold start:** tudo roda em _free tier_ (R$ 0). No plano free do
> Render o backend **hiberna após ~15 min** sem tráfego; o primeiro acesso
> seguinte demora **~30–50s** para responder. Veja [Keep-alive](#5-keep-alive-opcional-mas-recomendado)
> para evitar isso no dia da entrevista.

> **Dica de latência:** cada query passa app→banco, então mantenha os dois o mais
> perto possível. O Neon deste projeto está em **sa-east-1 (São Paulo)**; como o
> free do Render não tem região em SP, o `render.yaml` usa **virginia (us-east)**,
> a mais próxima disponível.

---

## Pré-requisitos

- Conta no [GitHub](https://github.com) com este repositório publicado.
- Conta no [Neon](https://neon.tech), no [Render](https://render.com) e na
  [Vercel](https://vercel.com) (dá para entrar com o GitHub nos três).

---

## 1. Banco de dados — Neon (PostgreSQL)

1. Crie um projeto no Neon (o deste projeto está em **AWS South America (São Paulo)**).
2. Nome do banco: `barbearia` (ou o que preferir).
3. Abra **Connection Details** e copie os dados. O Neon mostra algo como:
   ```
   postgresql://barbearia_owner:SENHA@ep-xxxx.us-west-2.aws.neon.tech/barbearia?sslmode=require
   ```
4. Traduza para o formato que o Spring espera (prefixo `jdbc:`):
   - `DB_URL` = `jdbc:postgresql://ep-xxxx.us-west-2.aws.neon.tech/barbearia?sslmode=require`
   - `DB_USER` = `barbearia_owner`
   - `DB_PASSWORD` = a senha
5. Não precisa criar tabelas: o **Flyway** roda as migrações `V1..V5` no primeiro
   boot do backend e semeia o admin.

---

## 2. Backend — Render (Docker)

Há um **Blueprint** pronto (`render.yaml`) na raiz do repositório.

1. No Render: **New +** → **Blueprint** → conecte este repositório.
2. O Render lê o `render.yaml` e cria o serviço web `barbearia-api` a partir de
   `backend/Dockerfile`.
3. Em **Environment**, preencha os valores (os que estão como _sync: false_):
   | Variável | Valor |
   |---|---|
   | `DB_URL` | o `jdbc:postgresql://...` do passo 1 |
   | `DB_USER` | usuário do Neon |
   | `DB_PASSWORD` | senha do Neon |
   | `JWT_SECRET` | clique em **Generate** (o Render cria uma chave forte) |
   | `CORS_ORIGINS` | preencha depois do passo 4 (URL da Vercel) |
4. **Create** e aguarde o build (Maven baixa dependências na 1ª vez → alguns minutos).
5. Ao final, o Render dá uma URL pública, ex.: `https://barbearia-api.onrender.com`.
   Teste no navegador: abrir a raiz deve responder (um 404 do Spring já indica que
   está no ar). **Guarde essa URL** para o passo 3.

> Sem `render.yaml` também dá: **New +** → **Web Service** → repositório →
> _Runtime: Docker_, _Root Directory: `backend`_, e defina as mesmas variáveis.

---

## 3. Apontar o frontend para o backend

Edite **`frontend/vercel.json`** e troque o host do `destination` pela sua URL do
Render (passo 2.5):

```json
{ "source": "/api/:path*", "destination": "https://SUA-API.onrender.com/api/:path*" }
```

Faça commit e push dessa mudança.

---

## 4. Frontend — Vercel (Angular)

1. Na Vercel: **Add New** → **Project** → importe este repositório.
2. Em **Root Directory**, selecione **`frontend`**.
   O framework é detectado como Angular; o `frontend/vercel.json` já define:
   - Build: `npm run build`
   - Output: `dist/frontend/browser`
   - Rewrites: `/api/*` → Render, e SPA fallback → `index.html`
3. **Deploy**. Ao final você recebe uma URL, ex.: `https://barbearia.vercel.app`.

### Fechar o CORS (defesa extra)

O caminho principal usa o proxy (sem CORS), mas configure mesmo assim, caso algo
chame o backend direto: volte ao Render e defina
`CORS_ORIGINS = https://barbearia.vercel.app`, salve e deixe reimplantar.

---

## 5. Keep-alive (opcional, mas recomendado)

Para o backend não hibernar (e evitar o cold start na frente do recrutador),
crie um ping periódico gratuito em [cron-job.org](https://cron-job.org):

- **URL:** a raiz do backend no Render (`https://SUA-API.onrender.com/`)
- **Intervalo:** a cada 10 minutos

Qualquer requisição (mesmo respondendo 404) reseta o timer de hibernação do Render.

---

## 6. Testar o demo

Abra a URL da Vercel e:

- **Cadastre-se** (nome, e-mail, senha, telefone) e crie/cancele agendamentos.
- **Entre como admin** para ver o painel administrativo:

  > **Admin do demo:** `admin@barbearia.com` / `Barbearia@Admin2026`
  > (semeado pela `V4`, senha definida pela `V6`). Como fica documentada aqui,
  > troque-a caso queira restringir de fato o acesso ao painel.

---

## Resumo das variáveis

Referência rápida em [`.env.example`](.env.example). Nenhum segredo fica no
repositório — todos são definidos nos painéis do Render (backend) e Neon (banco).

| Onde | Variáveis |
|---|---|
| **Render** (backend) | `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `CORS_ORIGINS` |
| **Neon** (banco) | fornece a connection string usada acima |
| **Vercel** (frontend) | nenhuma — a URL do backend fica no `frontend/vercel.json` |

## Solução de problemas

- **Frontend abre, mas login/lista falham:** confira o `destination` em
  `frontend/vercel.json` (URL correta do Render, com `/api/:path*` no fim).
- **Backend não sobe:** veja os logs no Render. Erros comuns: `DB_URL` sem o
  prefixo `jdbc:` ou sem `?sslmode=require`; usuário/senha do Neon trocados.
- **Primeiro acesso muito lento:** é o cold start do free tier — configure o
  [keep-alive](#5-keep-alive-opcional-mas-recomendado).
