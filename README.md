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
   Traefik v2.11  (reverse proxy, porta 80/443)
   ├── /api/*      ──► Backend Spring Boot  (porta 8080, prefixo removido)
   ├── /lab/sqli   ──► desafio-sqli         (porta 80)
   ├── /lab/xss    ──► desafio-xss          (porta 80)
   ├── /lab/fonte-secreta ──► desafio-fonte-secreta
   ├── /lab/cmd-injection ──► desafio-cmd-injection
   ├── /lab/jwt-inseguro  ──► desafio-jwt-inseguro
   └── /*          ──► Frontend Angular     (nginx, prioridade mais baixa)
        │
   Backend ──► PostgreSQL  (banco principal)
   desafio-sqli ──► MySQL  (banco isolado do desafio)
```

A rota do frontend é um catch-all (`PathPrefix('/')`) com `priority=1`, a mais baixa. Sem essa prioridade explícita ela engoliria `/api` e `/lab/*`, e nada além da página inicial funcionaria.

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

Os dois repositórios precisam ficar lado a lado — os composes referenciam o frontend por caminho relativo (`../UniHackFrontend/...`):

```
pasta-de-trabalho/
├── UniHack/            # este repositório
└── UniHackFrontend/    # repositório do frontend Angular
```

```
UniHack/
├── docker-compose.yml          # Produção (HTTPS, Let's Encrypt)
├── docker-compose.dev.yml      # Desenvolvimento local (HTTP) — sobe tudo, 10 containers
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

### 2. Escolha o modo de execução

O `docker-compose.dev.yml` sobe **tudo**: Traefik, PostgreSQL, MySQL, backend, frontend e os cinco laboratórios — 10 containers. Nenhuma variável de ambiente é necessária.

Há dois modos, e a escolha depende do que você vai mexer:

| | **Tudo em Docker** | **Híbrido (hot reload)** |
|---|---|---|
| Comando | `up -d` e pronto | `up -d` + `mvnw` + `npm start` |
| Acesso | tudo em `http://localhost` | front em `:4200`, back em `:8080` |
| Mudou o código | precisa rebuildar a imagem | recarrega sozinho |
| Bom para | rodar a plataforma, testar o conjunto | desenvolver backend ou frontend |

> ⚠️ **O `-f docker-compose.dev.yml` é obrigatório.** Sem ele, o Docker Compose usa o `docker-compose.yml`, que é o de **produção** — exige um `.env` e um domínio público com HTTPS, que não funciona em localhost.

#### Modo A — tudo em Docker

```bash
docker compose -f docker-compose.dev.yml up --build -d
```

O primeiro build demora alguns minutos (o backend compila com Maven e o frontend roda `npm ci` dentro da imagem). Depois disso o cache de camadas deixa tudo rápido.

Verifique:

```bash
docker compose -f docker-compose.dev.yml ps
```

Os **10** containers devem aparecer com status `running`. O Traefik roteia tudo pela porta 80:

| Caminho | Vai para |
|---|---|
| `http://localhost/` | frontend (Angular servido por nginx) |
| `http://localhost/api/*` | backend (o prefixo `/api` é removido pelo Traefik) |
| `http://localhost/lab/*` | containers dos desafios |

O PostgreSQL fica em `localhost:5433` para inspeção.

> Use sempre `-d`. Sem ele o compose fica anexado ao terminal e `Ctrl+C` **derruba todos os containers**. Para acompanhar os logs sem esse risco, veja [Acompanhando os logs](#acompanhando-os-logs).

#### Modo B — híbrido, com hot reload

Suba só a infraestrutura e os labs, deixando backend e frontend de fora:

```bash
docker compose -f docker-compose.dev.yml up -d \
  traefik db sqli-db \
  desafio-sqli desafio-xss desafio-fonte-secreta \
  desafio-cmd-injection desafio-jwt-inseguro
```

Backend, em outro terminal:

```bash
cd unihack-backend/unihack
./mvnw spring-boot:run
# sobe em http://localhost:8080
curl http://localhost:8080/auth/register -s -o /dev/null -w "%{http_code}"
# Esperado: 400 (rota existe, mas sem body)
```

Frontend (repositório separado), em outro terminal:

```bash
npm install
npm start
# abre em http://localhost:4200
```

Neste modo o frontend usa `environment.ts` (desenvolvimento), que aponta para `http://localhost:8080` e `http://localhost/lab` — endereços diretos, sem passar pelo `/api` do Traefik.

### 3. Cadastre os desafios no banco

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

Se retornar `0`, execute o passo [Cadastre os desafios no banco](#3-cadastre-os-desafios-no-banco).

---

### `unable to prepare context: ... Dockerfile: no such file or directory`

```
unable to prepare context: unable to evaluate symlinks in Dockerfile path:
lstat .../UniHackFrontend/unihack-frontend/Dockerfile: no such file or directory
```

**Causa:** o repositório do frontend não está clonado ao lado do backend. Ambos os composes esperam esta disposição:

```
pasta-de-trabalho/
├── UniHack/            # este repositório
└── UniHackFrontend/    # repositório do frontend
```

O `context: ../UniHackFrontend/unihack-frontend` sobe um nível a partir de `UniHack/`, então clonar o frontend dentro de outra pasta quebra o build.

**Aparece junto:** vários `WARN ... variable is not set`. Isso indica que você rodou sem `-f` e o Compose pegou o `docker-compose.yml` (produção), que espera um `.env`. Para desenvolvimento local use sempre `-f docker-compose.dev.yml`.

---

### `Application bundle generation failed` — budget exceeded

```
✘ [ERROR] src/app/pages/perfil/perfil.component.scss exceeded maximum budget.
```

**Causa:** a configuração **production** do Angular impõe limites de tamanho que a **development** não impõe. Como `npm start` usa development, o problema só aparece ao buildar a imagem Docker.

**Solução:** ajuste os `budgets` em `angular.json` (já foi feito neste repositório) ou reduza o CSS do componente citado.

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

Ambos os composes têm volume nomeado para o Postgres (`db-dev-data` em dev, `db-data` em produção), então um `down` comum **preserva** usuários, desafios e pontuações.

**Se sumiram mesmo,** foi um destes:

| Comando | Efeito |
|---|---|
| `docker compose down` | Remove containers. Volume **preservado**. |
| `docker compose down -v` | Remove containers **e volumes**. Apaga tudo. |
| `docker volume rm unihack_db-dev-data` | Apaga o banco de dev diretamente. |

O `-v` é o culpado quase sempre. Para recuperar, refaça o passo [Cadastre os desafios no banco](#3-cadastre-os-desafios-no-banco) e recrie o admin.

**Backup e restauração** — vale antes de qualquer operação destrutiva:

```bash
# Backup
docker exec unihack-db-1 pg_dump -U postgres -d unihack --clean --if-exists > backup.sql

# Restauração
docker exec -i unihack-db-1 psql -U postgres -d unihack < backup.sql
```

> ⚠️ **Ao adicionar um volume a um banco que já tem dados**, o volume novo começa vazio e o Postgres inicializa um banco limpo — os dados antigos ficam órfãos na camada do container anterior. Faça o `pg_dump` **antes** de recriar o container e restaure depois.

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
| Validação de username duplicado | Comentada em `AuthController.java` (bloco logo após a checagem de matrícula). O método `existsByUsername` **também não existe** no `UserRepository`, então descomentar sozinho não compila — é preciso adicionar `boolean existsByUsername(String username);` à interface. |
| Painel admin | Não existe. Desafios só podem ser criados via `curl`/SQL. |
| Interface do JWT Inseguro | O desafio é só API Flask, sem UI. |
| Seed de desafios | Manual. Não há migration nem script versionado — o SQL está neste README. |
| Budgets de CSS | `perfil.component.scss` (18 kB) e `ranking.component.scss` (8 kB) estouravam o limite padrão do Angular e **quebravam a build de produção**. Os budgets em `angular.json` foram afrouxados (erro em 24 kB) para destravar; o certo seria enxugar esses SCSS. |
| `nginx/nginx.conf` (raiz) | Arquivo vazio e sem uso. O nginx do frontend usa `UniHackFrontend/unihack-frontend/nginx.conf`. Pode ser removido. |
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

### 2. Confira o build do frontend

Nada a fazer aqui — o `Dockerfile`, o `nginx.conf` e o `.dockerignore` já existem em `UniHackFrontend/unihack-frontend/`, e são os mesmos usados no compose de dev. Vale só entender três decisões, porque quebram de formas confusas se forem mexidas:

**1. O `COPY` termina em `/browser`.** O `outputPath` do `angular.json` é `dist/unihack-frontend`, mas o builder `application` do Angular 19 separa a saída do navegador em um subdiretório. Copiar `dist/unihack-frontend` direto serve a pasta errada e dá 404 em tudo.

**2. O `nginx.conf` tem fallback de SPA.** Sem o `try_files $uri $uri/ /index.html`, acessar `/ranking` direto (ou dar F5 fora da home) devolve 404 — o roteamento é do Angular, e o nginx procura um arquivo que não existe.

**3. As URLs de produção são relativas.** O `angular.json` tem um `fileReplacements` que troca `environment.ts` por `environment.prod.ts` na configuração de produção:

```typescript
export const environment = {
  production: true,
  apiUrl: '/api',
  labBaseUrl: '/lab',
};
```

Isso é essencial. O `environment.ts` de desenvolvimento aponta para `http://localhost:8080`, que fica **assado no bundle** — se ele fosse usado no build de produção, a página serviria um JavaScript que tenta chamar `localhost:8080` no navegador de quem acessa, e nada funcionaria fora da sua máquina. Com caminhos relativos, o mesmo bundle serve qualquer domínio.

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

E cadastre os desafios — o banco de produção também começa vazio. Use o SQL do passo [Cadastre os desafios no banco](#3-cadastre-os-desafios-no-banco), trocando `-U postgres` pelo `$POSTGRES_USER` do seu `.env`.

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

O frontend Angular está em um repositório separado: [UniHackFrontend](https://github.com/Isaac-code-maker/UniHackFrontend). **Clone-o ao lado do `UniHack/`**, não dentro — os dois composes referenciam `../UniHackFrontend/unihack-frontend`.

São dois arquivos de ambiente, e o Angular troca um pelo outro conforme a configuração de build (`fileReplacements` no `angular.json`):

| Arquivo | Usado em | `apiUrl` | `labBaseUrl` |
|---|---|---|---|
| `environment.ts` | `npm start` (development) | `http://localhost:8080` | `http://localhost/lab` |
| `environment.prod.ts` | build de produção / imagem Docker | `/api` | `/lab` |

O de desenvolvimento fala direto com o backend na porta 8080. O de produção usa caminhos relativos porque tudo passa pelo Traefik no mesmo domínio — assim o mesmo bundle funciona em localhost, em staging e no domínio real, sem rebuild.

> Se você adicionar uma chave nova ao `environment.ts`, adicione também ao `environment.prod.ts`. O TypeScript não avisa quando um dos dois fica para trás, e o sintoma é `undefined` só em produção.

Arquivos relacionados no repositório do frontend:

```
unihack-frontend/
├── Dockerfile           # build multi-stage: npm ci → nginx:alpine
├── nginx.conf           # fallback de SPA + cache dos assets
├── .dockerignore        # evita mandar node_modules/dist no contexto
└── src/environments/
    ├── environment.ts
    └── environment.prod.ts
```

---

## Licença

Este projeto está licenciado sob a [MIT License](LICENSE).
