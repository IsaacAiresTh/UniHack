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
- [Problemas comuns](#problemas-comuns)
- [Limitações conhecidas](#limitações-conhecidas)
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
- **Node.js** >= 20 (para o frontend Angular)
- Portas livres: **80** (Traefik), **5433** (PostgreSQL), **8080** (backend), **4200** (frontend)

### Não use `sudo`

Adicione seu usuário ao grupo `docker` e faça logout/login:

```bash
sudo usermod -aG docker $USER
```

Confirme com `id` — deve aparecer `docker` na lista de grupos. Rodar com `sudo` ignora o seu `~/.docker` e cria containers com dono `root`, o que atrapalha na hora de limpar.

### Plugin buildx

Se aparecer `WARN Docker Compose requires buildx plugin to be installed` seguido de erros `Can't add file ... to tar: io: read/write on closed pipe`, falta o buildx. O builder legado ainda funciona como fallback, mas instale para evitar ruído:

```bash
# Arch
sudo pacman -S docker-buildx

# Debian/Ubuntu
sudo apt install docker-buildx-plugin
```

---

## Rodando localmente

### 1. Clone o repositório

```bash
git clone https://github.com/Isaac-code-maker/UniHack.git
cd UniHack
```

### 2. Suba a infraestrutura com Docker Compose

O arquivo `docker-compose.dev.yml` sobe o Traefik, o PostgreSQL, o MySQL e todos os cinco laboratórios de desafio. Nenhuma variável de ambiente é necessária — tudo usa valores padrão para dev.

> ⚠️ **O `-f docker-compose.dev.yml` é obrigatório.** Sem ele, o Docker Compose usa o `docker-compose.yml`, que é o de **produção** — ele exige um `.env`, um domínio público e o `Dockerfile` do frontend (que [ainda não existe](#limitações-conhecidas)). O build falha com:
>
> ```
> unable to prepare context: unable to evaluate symlinks in Dockerfile path:
> lstat .../UniHackFrontend/unihack-frontend/Dockerfile: no such file or directory
> ```

```bash
docker compose -f docker-compose.dev.yml up --build -d
```

Verifique se os containers estão de pé:

```bash
docker compose -f docker-compose.dev.yml ps
```

Todos os **8** containers devem aparecer com status `running` (Traefik, PostgreSQL, MySQL e os 5 labs). O PostgreSQL fica acessível em `localhost:5433`.

> Use sempre `-d`. Sem ele o compose fica anexado ao terminal e `Ctrl+C` **derruba todos os containers**. Para acompanhar os logs sem esse risco, veja [Acompanhando os logs](#acompanhando-os-logs).

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

### 5. Cadastre os desafios no banco

> ⚠️ **Passo obrigatório.** Subir os containers **não** cadastra os desafios. Os labs e os registros na tabela `challenges` são coisas separadas: o Docker sobe os laboratórios, mas o banco começa vazio. Se pular esta etapa, a tela de CTFs mostra *"Nenhum desafio encontrado com os filtros atuais"* — mensagem enganosa, porque o problema não é filtro nem login, é banco vazio.

A forma mais rápida é inserir direto no Postgres. O `flag_hash` é o MD5 da flag em texto puro, exatamente como o `ChallengeController` gera (`DigestUtils.md5DigestAsHex`):

```bash
docker exec -i unihack-db-1 psql -U postgres -d unihack <<'SQL'
INSERT INTO challenges (id, title, description, difficulty, score, slug, flag_hash, category) VALUES
(gen_random_uuid(), 'SQL Injection',
 'Um sistema de login vulneravel esta disponivel. Encontre a flag sem saber a senha.',
 'Facil', 100, 'sqli', '1b7d9217ee671ab72340a13ccebec910', 'Web'),
(gen_random_uuid(), 'Cross-Site Scripting',
 'A aplicacao reflete a entrada do usuario sem sanitizar. Execute seu proprio JavaScript na pagina.',
 'Facil', 100, 'xss', '321cf024ebf037628605460f087f2134', 'Web'),
(gen_random_uuid(), 'Fonte Secreta',
 'Nem tudo que esta na pagina aparece na tela. Inspecione o codigo-fonte.',
 'Muito Facil', 50, 'fonte-secreta', '4980b926ca933a8657a48220715c17b5', 'Web'),
(gen_random_uuid(), 'Command Injection',
 'A ferramenta de ping executa comandos no servidor. Encadeie seu proprio comando e leia o arquivo da flag.',
 'Medio', 150, 'cmd-injection', 'd936ad80f9b2fb03eadc30e730e86794', 'Web'),
(gen_random_uuid(), 'JWT Inseguro',
 'A API emite tokens JWT assinados com um segredo fraco. Forje um token de administrador.',
 'Medio', 150, 'jwt-inseguro', '0d2a271031364bb1bcc4cc6dbf0f263b', 'Web');
SQL
```

Confirme:

```bash
docker exec unihack-db-1 psql -U postgres -d unihack -c "SELECT title, slug, score FROM challenges;"
# Esperado: 5 linhas
```

Alternativa: cadastre via API com um token de ADMIN — veja [Cadastrando desafios](#cadastrando-desafios).

### Acompanhando os logs

**Containers:**

```bash
docker compose -f docker-compose.dev.yml logs -f            # todos
docker compose -f docker-compose.dev.yml logs -f db         # só o Postgres
docker compose -f docker-compose.dev.yml logs -f desafio-sqli
```

**Backend e frontend** rodam fora do Docker, então os logs saem no próprio terminal onde você executou `./mvnw spring-boot:run` e `npm start`. Se preferir rodá-los em background, redirecione para um arquivo e use `tail -f`:

```bash
./mvnw spring-boot:run > backend.log 2>&1 &
tail -f backend.log | grep -E --line-buffered "ERROR|Exception|WARN"
```

---

## Criando o primeiro admin

O registro público cria usuários com role `USER`. Para promover um usuário a `ADMIN`, acesse o banco diretamente:

```bash
docker exec -it unihack-db-1 \
  psql -U postgres -d unihack \
  -c "UPDATE users SET role = 'ADMIN' WHERE matricula = 'SUA_MATRICULA';"
```

> Substitua `SUA_MATRICULA` pela matrícula do usuário que deve ser admin. Confirme com `SELECT username, matricula, role FROM users;`.

O nome do container é `unihack-db-1` porque o compose deriva de `<pasta>_<serviço>_<n>` — se você renomear a pasta do projeto, o nome muda. Nesse caso descubra com `docker compose -f docker-compose.dev.yml ps`.

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

| Título | Slug | Dificuldade | Pontos | Categoria | Flag |
|---|---|---|---|---|---|
| SQL Injection | `sqli` | Fácil | 100 | Web | `FLAG{sql_injection_concluido_com_sucesso}` |
| Cross-Site Scripting | `xss` | Fácil | 100 | Web | `FLAG{xss_dom_exploitation_ftw}` |
| Fonte Secreta | `fonte-secreta` | Muito Fácil | 50 | Web | `FLAG{inspecionar_eh_o_primeiro_passo}` |
| Command Injection | `cmd-injection` | Médio | 150 | Web | `FLAG{o_ponto_e_virgula_eh_poderoso}` |
| JWT Inseguro | `jwt-inseguro` | Médio | 150 | Web | `FLAG{jwt_forjado_com_sucesso}` |

> ⚠️ **O slug precisa bater com a rota do Traefik, não com o nome da pasta.** O desafio de command injection fica em `desafios/command-injection/`, mas o slug é **`cmd-injection`** — é o que está no `PathPrefix` do compose. O frontend monta a URL do lab como `labBaseUrl + slug`, então um slug errado gera um botão que leva a 404.
>
> A fonte da verdade das flags é o código do próprio desafio (`flag.txt`, `index.js`, `app.py`, `index.html`), não esta tabela. Para conferir todas de uma vez:
>
> ```bash
> grep -rhoE "FLAG\{[^}]+\}" desafios/ | sort -u
> ```

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
| JWT Inseguro | http://localhost/lab/jwt-inseguro/login (POST) · /admin |

> **O JWT Inseguro é só API — não tem interface.** O Flask expõe apenas `POST /login` e `GET /admin`; não existe rota `/`. Um **404 em `http://localhost/lab/jwt-inseguro` é o comportamento esperado**, não um container quebrado. Para testar:
>
> ```bash
> curl -X POST http://localhost/lab/jwt-inseguro/login \
>   -H "Content-Type: application/json" -d '{"username":"teste"}'
> ```

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

## Problemas comuns

### "Nenhum desafio encontrado com os filtros atuais"

A tela de CTFs aparece vazia mesmo com login feito e todos os containers de pé.

**Causa:** a tabela `challenges` está vazia. A mensagem fala em "filtros" e despista — não tem nada a ver com filtro, busca ou autenticação. Subir os containers não cadastra os desafios.

**Diagnóstico:**
```bash
docker exec unihack-db-1 psql -U postgres -d unihack -c "SELECT count(*) FROM challenges;"
```

Se retornar `0`, execute o passo [Cadastre os desafios no banco](#5-cadastre-os-desafios-no-banco).

---

### `unable to prepare context: ... Dockerfile: no such file or directory`

```
unable to prepare context: unable to evaluate symlinks in Dockerfile path:
lstat .../UniHackFrontend/unihack-frontend/Dockerfile: no such file or directory
```

**Causa:** você rodou `docker compose build` sem `-f`, e o Compose pegou o `docker-compose.yml` (produção), que referencia um `Dockerfile` de frontend que ainda não existe.

**Solução:** para desenvolvimento local, use sempre `-f docker-compose.dev.yml`. Os `WARN ... variable is not set` que aparecem junto são do mesmo problema — o compose de produção espera um `.env`.

---

### Erro 500 ao se cadastrar

O log do backend mostra:

```
ERROR: duplicate key value violates unique constraint "ukr43af9ap4edm43mmtq01oddj6"
  Detalhe: Key (username)=(Fulano) already exists.
```

**Causa:** o campo `username` tem constraint `UNIQUE` no model, mas a validação está comentada no `AuthController` — veja [Limitações conhecidas](#limitações-conhecidas). Matrícula duplicada devolve um 409 tratado; **nome** duplicado estoura uma `DataIntegrityViolationException` e vira 500 genérico.

**Contorno:** use outro nome, ou remova o usuário conflitante:

```bash
docker exec unihack-db-1 psql -U postgres -d unihack \
  -c "DELETE FROM users WHERE username = 'Fulano';"
```

---

### Os dados sumiram depois de um `docker compose down`

**Causa:** o serviço `db` do `docker-compose.dev.yml` **não tem volume**. Os dados vivem na camada gravável do container, então `down` apaga usuários, desafios e pontuações. (O compose de produção *tem* volume — `db-data`.)

**Contorno:** use `stop`/`start` em vez de `down`/`up` para preservar os dados entre sessões. Se derrubou mesmo, refaça o passo [Cadastre os desafios no banco](#5-cadastre-os-desafios-no-banco) e recrie o admin.

**Correção definitiva:** adicionar um volume ao serviço `db` no `docker-compose.dev.yml`:

```yaml
  db:
    image: postgres:15
    volumes:
      - db-dev-data:/var/lib/postgresql/data
    # ...

volumes:
  db-dev-data:
```

---

### `Ctrl+C` derrubou tudo

`docker compose up` **sem `-d`** fica anexado ao terminal, e `Ctrl+C` para todos os containers. Suba com `-d` e acompanhe os logs com `logs -f`.

---

### Rebuild não resolveu

`--no-cache` reconstrói as 5 imagens do zero (leva ~1 min, recompila a extensão `mysqli` do PHP) e **não afeta o banco**. Se o sintoma é dado faltando ou duplicado — desafio não aparece, usuário já existe — o problema está no Postgres, não na imagem. Rebuild só ajuda quando você mudou um `Dockerfile` ou o código de um desafio.

Para o dia a dia, `up -d --build` basta: reconstrói só o que mudou.

---

## Limitações conhecidas

Coisas que ainda não estão prontas — vale saber antes de gastar tempo investigando.

| Item | Situação |
|---|---|
| `Dockerfile` do frontend | **Não existe.** O `docker-compose.yml` de produção referencia `UniHackFrontend/unihack-frontend/Dockerfile`, e o build falha sem ele. Há um exemplo em [Deploy em produção](#2-construa-o-frontend-para-produção). |
| `nginx/nginx.conf` | Arquivo vazio (0 bytes). Precisa de fallback SPA (`try_files`) para as rotas do Angular funcionarem em deep link. |
| Validação de username duplicado | Comentada em `AuthController.java` (bloco logo após a checagem de matrícula). O método `existsByUsername` **também não existe** no `UserRepository`, então descomentar sozinho não compila — é preciso adicionar `boolean existsByUsername(String username);` à interface. |
| Painel admin | Não existe. Desafios só podem ser criados via `curl`/SQL. |
| Interface do JWT Inseguro | O desafio é só API Flask, sem UI. |
| Seed de desafios | Manual. Não há migration nem script versionado — o SQL está neste README. |
| Deploy em VPS | Ainda não foi feito. |

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

> ⚠️ **Este passo é obrigatório e ainda não foi feito no repositório.** O `docker-compose.yml` referencia `UniHackFrontend/unihack-frontend/Dockerfile`, que não existe — sem criá-lo, o build de produção falha antes de qualquer outra coisa.

Crie um `Dockerfile` em `UniHackFrontend/unihack-frontend/` que gere o build estático do Angular e o sirva com nginx:

```dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration=production

FROM nginx:alpine
COPY --from=build /app/dist/unihack-frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

O `outputPath` no `angular.json` é `dist/unihack-frontend`, e o Angular 19 coloca os arquivos do browser em um subdiretório `browser/` — por isso o caminho do `COPY` termina em `/browser`.

O `nginx.conf` **precisa** do fallback para SPA, senão qualquer acesso direto a uma rota (`/ranking`, `/perfil`, ou um F5 fora da home) devolve 404, porque o roteamento é do Angular e o nginx procura um arquivo que não existe:

```nginx
server {
    listen 80;
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

> O arquivo `nginx/nginx.conf` na raiz deste repositório está vazio (0 bytes) e não é usado por nenhum serviço do compose.

Lembre também de apontar o `environment.ts` de produção para o domínio real (`https://SEU_DOMINIO` e `https://SEU_DOMINIO/lab`) — em produção o backend fica atrás de `/api`, diferente do dev.

### 3. Suba com o compose de produção

```bash
docker compose up --build -d
```

Aqui o `-f` é dispensável: sem ele o Compose usa o `docker-compose.yml`, que é o de produção mesmo.

O Traefik cuida automaticamente dos certificados SSL via Let's Encrypt. HTTP é redirecionado para HTTPS.

### 4. Crie o admin em produção

```bash
docker exec -it unihack-db-1 \
  psql -U $POSTGRES_USER -d unihack \
  -c "UPDATE users SET role = 'ADMIN' WHERE matricula = 'SUA_MATRICULA';"
```

E cadastre os desafios — o banco de produção também começa vazio. Use o SQL do passo [Cadastre os desafios no banco](#5-cadastre-os-desafios-no-banco), trocando `-U postgres` pelo `$POSTGRES_USER` do seu `.env`.

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
