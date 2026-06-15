# UniHack

Plataforma de **Capture The Flag (CTF)** desenvolvida para os alunos dos cursos de **Engenharia de Software e ADS da UNICEPLAC**. O objetivo é fomentar o aprendizado prático de segurança da informação por meio de desafios técnicos, ranking em tempo real e laboratórios interativos acessíveis diretamente pelo navegador.

---

## Sumário

- [Arquitetura](#arquitetura)
- [Stack](#stack)
- [Estrutura do Repositório](#estrutura-do-repositório)
- [Pré-requisitos](#pré-requisitos)
- [Rodando localmente](#rodando-localmente)
- [Criando o primeiro admin](#criando-o-primeiro-admin)
- [Cadastrando desafios](#cadastrando-desafios)
- [Testando o fluxo completo](#testando-o-fluxo-completo)
- [Deploy em produção](#deploy-em-produção)
- [Referência da API](#referência-da-api)
- [Frontend](#frontend)

---

## Arquitetura

O UniHack usa um modelo de **instâncias compartilhadas por desafio**: cada laboratório roda como um container Docker fixo, sempre ativo, compartilhado por todos os usuários simultaneamente. Isso substitui o modelo anterior de container-por-sessão, que não escalava para múltiplos usuários e exigia Docker-in-Docker na aplicação.

```
Usuário (navegador)
        │
        ▼
   Traefik v2.11  (reverse proxy)
   ├── /api/*      ──► Backend Spring Boot  (porta 8080)
   ├── /lab/sqli   ──► desafio-sqli         (porta 80)
   ├── /lab/xss    ──► desafio-xss          (porta 80)
   ├── /lab/fonte-secreta ──► desafio-fonte-secreta
   ├── /lab/cmd-injection ──► desafio-cmd-injection
   └── /lab/jwt-inseguro  ──► desafio-jwt-inseguro
        │
   Backend ──► PostgreSQL  (banco principal)
   desafio-sqli ──► MySQL  (banco isolado do desafio)
```

**Flags** são armazenadas no banco como hash MD5 — nunca em texto puro. Na submissão, o backend faz o hash da entrada do usuário e compara. Submissões duplicadas são bloqueadas por uma constraint `UNIQUE(user_id, challenge_id)` na tabela `solved_challenges`.

---

## Stack

| Camada | Tecnologia |
|---|---|
| Backend | Spring Boot 3.4.4 · Java 21 |
| Segurança | Spring Security · JWT (Bearer token) |
| Banco principal | PostgreSQL 15 |
| Banco do desafio SQLi | MySQL 8.0 |
| Proxy reverso | Traefik v2.11 |
| Containerização | Docker · Docker Compose |
| Frontend | Angular 19 (repositório separado) |

---

## Estrutura do Repositório

```
UniHack/
├── docker-compose.yml          # Produção (HTTPS, Let's Encrypt)
├── docker-compose.dev.yml      # Desenvolvimento local (HTTP)
├── .env.example                # Variáveis de ambiente necessárias em produção
├── desafios/
│   ├── sqli/                   # PHP + MySQL — SQL Injection
│   ├── xss/                    # Node.js — Cross-Site Scripting
│   ├── fonte-secreta/          # HTML estático — inspecionar código-fonte
│   ├── command-injection/      # Node.js — Command Injection
│   └── jwt-inseguro/           # Python/Flask — JWT forjado
└── unihack-backend/
    └── unihack/                # Projeto Spring Boot (Maven)
        ├── src/main/java/com/unihack/unihack/
        │   ├── controllers/    # AuthController, ChallengeController, UserController
        │   ├── models/         # User, Challenge, SolvedChallenge
        │   ├── dtos/           # DTOs de entrada e saída
        │   ├── repository/     # Interfaces JPA
        │   ├── services/       # UsersService, UserDetailsServiceImpl
        │   └── configs/        # SecurityConfig, JWT filter/provider
        └── src/main/resources/
            └── application.properties
```

---

## Pré-requisitos

- **Docker** >= 24 e **Docker Compose** plugin (`docker compose`)
- **Java 21** e **Maven** (ou use o `./mvnw` incluso no projeto)
- Porta **80** livre (Traefik dev) e **5433** livre (PostgreSQL exposto)

---

## Rodando localmente

### 1. Clone o repositório

```bash
git clone https://github.com/Isaac-code-maker/UniHack.git
cd UniHack
```

### 2. Suba a infraestrutura com Docker Compose

O arquivo `docker-compose.dev.yml` sobe o Traefik, o PostgreSQL, o MySQL e todos os cinco laboratórios de desafio. Nenhuma variável de ambiente é necessária — tudo usa valores padrão para dev.

```bash
docker compose -f docker-compose.dev.yml up --build -d
```

Verifique se os containers estão de pé:

```bash
docker compose -f docker-compose.dev.yml ps
```

Todos devem aparecer com status `running`. O PostgreSQL fica acessível em `localhost:5433`.

### 3. Rode o backend

Em outro terminal, dentro de `unihack-backend/unihack/`:

```bash
cd unihack-backend/unihack
./mvnw spring-boot:run
```

O backend sobe em `http://localhost:8080`. Para confirmar:

```bash
curl http://localhost:8080/auth/register -s -o /dev/null -w "%{http_code}"
# Esperado: 400 (rota existe, mas sem body)
```

### 4. Clone e rode o frontend

O frontend está em um repositório separado. Após clonar:

```bash
# No diretório do frontend
npm install
npm start
```

A aplicação abre em `http://localhost:4200` (ou outra porta se 4200 estiver ocupada — o Angular exibirá a porta correta no terminal).

---

## Criando o primeiro admin

O registro público cria usuários com role `USER`. Para promover um usuário a `ADMIN`, acesse o banco diretamente:

```bash
docker exec -it $(docker ps -qf "name=unihack-db-1") \
  psql -U postgres -d unihack \
  -c "UPDATE users SET role = 'ADMIN' WHERE matricula = 'SUA_MATRICULA';"
```

> Substitua `SUA_MATRICULA` pela matrícula do usuário que deve ser admin.

---

## Cadastrando desafios

Apenas usuários com role `ADMIN` podem criar desafios. Use o token JWT do admin obtido no login:

```bash
TOKEN="cole_o_token_jwt_aqui"

curl -X POST http://localhost:8080/challenges/create \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "SQL Injection",
    "description": "Um sistema de login vulnerável está disponível. Encontre a flag sem saber a senha.",
    "difficulty": "Fácil",
    "score": 100,
    "slug": "sqli",
    "category": "Web",
    "flag": "FLAG{sql_injection_concluido_com_sucesso}"
  }'
```

A flag é recebida em texto puro e armazenada como hash MD5 — o valor original nunca fica no banco.

**Desafios padrão do projeto:**

| Título | Slug | Dificuldade | Pontos | Categoria |
|---|---|---|---|---|
| SQL Injection | `sqli` | Fácil | 100 | Web |
| Cross-Site Scripting | `xss` | Fácil | 100 | Web |
| Fonte Secreta | `fonte-secreta` | Muito Fácil | 50 | Web |
| Command Injection | `cmd-injection` | Médio | 150 | Web |
| JWT Inseguro | `jwt-inseguro` | Médio | 150 | Web |

---

## Testando o fluxo completo

### Registro e login

```bash
# Registro
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Isaac","matricula":"2410001","password":"senha123","confirmPassword":"senha123"}'

# Login — guarde o token retornado
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"matricula":"2410001","password":"senha123"}'
```

### Listando e consultando desafios

```bash
TOKEN="cole_o_token_aqui"

# Listar todos
curl http://localhost:8080/challenges/all \
  -H "Authorization: Bearer $TOKEN"

# Detalhes de um desafio (substitua o UUID)
curl http://localhost:8080/challenges/details/{id} \
  -H "Authorization: Bearer $TOKEN"

# Verificar se já resolveu
curl http://localhost:8080/challenges/{id}/status \
  -H "Authorization: Bearer $TOKEN"
```

### Acessando o laboratório

Com os containers rodando, os labs ficam disponíveis em:

| Desafio | URL local |
|---|---|
| SQL Injection | http://localhost/lab/sqli |
| XSS | http://localhost/lab/xss |
| Fonte Secreta | http://localhost/lab/fonte-secreta |
| Command Injection | http://localhost/lab/cmd-injection |
| JWT Inseguro | http://localhost/lab/jwt-inseguro |

### Submetendo uma flag

```bash
curl -X POST http://localhost:8080/challenges/submit \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"flag":"FLAG{sql_injection_concluido_com_sucesso}"}'
```

Resposta esperada na primeira submissão correta:
```json
{ "message": "Flag correta! +100 pontos adicionados." }
```

Tentativa duplicada:
```json
{ "message": "Você já resolveu este desafio anteriormente." }
```

### Ranking

```bash
curl http://localhost:8080/users/ranking \
  -H "Authorization: Bearer $TOKEN"
```

---

## Deploy em produção

### 1. Configure as variáveis de ambiente

Copie o arquivo de exemplo e preencha os valores reais:

```bash
cp .env.example .env
```

Edite o `.env`:

```env
DOMAIN=unihack.uniceplac.edu.br
FRONTEND_URL=https://unihack.uniceplac.edu.br
ACME_EMAIL=seu@email.com
POSTGRES_USER=unihack
POSTGRES_PASSWORD=senha_forte_aqui
SQLI_DB_PASSWORD=outra_senha_forte
JWT_SECRET=chave_aleatoria_longa_minimo_32_caracteres
```

### 2. Construa o frontend para produção

Antes de subir, gere o build estático do Angular e adicione um `Dockerfile` em `UniHackFrontend/unihack-frontend/` que sirva os arquivos com nginx. Exemplo de `Dockerfile`:

```dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration=production

FROM nginx:alpine
COPY --from=build /app/dist/unihack-frontend/browser /usr/share/nginx/html
EXPOSE 80
```

### 3. Suba com o compose de produção

```bash
docker compose up --build -d
```

O Traefik cuida automaticamente dos certificados SSL via Let's Encrypt. HTTP é redirecionado para HTTPS.

### 4. Crie o admin em produção

```bash
docker exec -it $(docker ps -qf "name=unihack-db-1") \
  psql -U $POSTGRES_USER -d unihack \
  -c "UPDATE users SET role = 'ADMIN' WHERE matricula = 'SUA_MATRICULA';"
```

---

## Referência da API

Todas as rotas (exceto `/auth/*`) exigem o header `Authorization: Bearer <token>`.

### Autenticação

| Método | Rota | Auth | Descrição |
|---|---|---|---|
| POST | `/auth/register` | Não | Registra novo usuário |
| POST | `/auth/login` | Não | Login, retorna JWT |

**Registro — body:**
```json
{
  "name": "string",
  "matricula": "string (7 dígitos)",
  "password": "string",
  "confirmPassword": "string"
}
```

**Login — body:**
```json
{ "matricula": "string", "password": "string" }
```

**Login — resposta:**
```json
{ "token": "jwt...", "message": "Login bem-sucedido!", "matricula": "..." }
```

---

### Desafios

| Método | Rota | Auth | Descrição |
|---|---|---|---|
| GET | `/challenges/all` | USER | Lista todos os desafios |
| GET | `/challenges/details/{id}` | USER | Detalhes de um desafio |
| GET | `/challenges/{id}/status` | USER | Verifica se o usuário já resolveu |
| POST | `/challenges/create` | ADMIN | Cria um desafio |
| POST | `/challenges/submit` | USER | Submete uma flag |

**Criação de desafio — body:**
```json
{
  "title": "string",
  "description": "string",
  "difficulty": "string",
  "score": 100,
  "slug": "string (único, usado na URL do lab)",
  "category": "string",
  "flag": "FLAG{texto_puro}"
}
```

**Submissão — body:**
```json
{ "flag": "FLAG{texto_submetido}" }
```

---

### Usuários

| Método | Rota | Auth | Descrição |
|---|---|---|---|
| GET | `/users/ranking` | USER | Ranking de todos os usuários |
| GET | `/users/me` | USER | Perfil do usuário autenticado |

---

## Frontend

O frontend Angular está em um repositório separado: [UniHackFrontend](https://github.com/Isaac-code-maker/UniHackFrontend).

Configure `src/environments/environment.ts` com:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080',
  labBaseUrl: 'http://localhost/lab',
};
```

Em produção, use `environment.prod.ts` com as URLs do domínio real.

---

## Licença

Este projeto está licenciado sob a [MIT License](LICENSE).
