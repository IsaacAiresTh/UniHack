# UniHack: Plataforma de CTF

**UniHack** é uma plataforma de *Capture The Flag* (CTF) gamificada, desenvolvida com o objetivo de fomentar a cultura de segurança da informação entre os alunos da **UNICEPLAC**. Através de desafios práticos e dinâmicos, rankings e um sistema de pontuação, a plataforma incentiva o aprendizado contínuo em um ambiente competitivo e colaborativo.

## 🚀 Diferencial do Projeto

O grande diferencial do projeto é a **criação de ambientes de desafios sob demanda usando Docker**, garantindo que cada participante tenha uma instância isolada e segura para resolver os problemas propostos.

## 🎯 Objetivos

- Promover o estudo de cibersegurança por meio de desafios técnicos e práticos
- Oferecer uma ferramenta escalável para a organização de eventos internos e competições de segurança
- Integrar uma plataforma de CTF moderna e dinâmica à grade extracurricular da instituição
- Estimular o trabalho em equipe e a troca de conhecimento entre os estudantes

## ⚙️ Tecnologias Utilizadas

| Categoria | Tecnologia |
|-----------|------------|
| **Back-end** | Java 21, Spring Boot 3 |
| **Front-end** | Angular (Planejado) |
| **Banco de Dados** | PostgreSQL |
| **Segurança** | Spring Security com JWT |
| **Containerização** | Docker e Docker Compose |
| **Integração** | Docker-Java para gerenciamento dinâmico |

## ✅ Funcionalidades Implementadas

### 🔐 Sistema de Autenticação
- Registro e Login de usuários com validação de dados
- Autenticação stateless baseada em tokens JWT
- Controle de acesso por papéis (`USER` e `ADMIN`)

### 🎮 Gerenciamento Dinâmico de Desafios
- API para listar, visualizar e criar desafios (administradores)
- **Iniciar desafios sob demanda**: Endpoint `/challenges/{id}/start` que inicia um contêiner Docker específico e retorna a URL de acesso
- **Submissão de flags**: Usuário envia a flag e ID do contêiner; se correto, ganha pontos e o contêiner é finalizado automaticamente

### 🏆 Sistema de Gamificação
- Sistema de pontuação baseado na resolução dos desafios
- API para exibir **ranking** de usuários ordenado por pontuação

## 🏗️ Estrutura do Projeto

```
unihack/
├── unihack-backend/unihack/        # Projeto principal do Spring Boot (API)
│   ├── src/main/java/              # Código-fonte Java
│   ├── pom.xml                     # Dependências e build do Maven
│   └── Dockerfile                  # Instruções para construir a imagem do back-end
├── desafios/                       # Diretório com os desafios práticos
│   └── sqli/                       # Exemplo de desafio de SQL Injection
│       ├── Dockerfile              # Imagem do ambiente do desafio (PHP + Apache)
│       ├── index.php               # Aplicação vulnerável
│       └── db/init.sql             # Schema do banco de dados do desafio
└── docker-compose.yml              # Orquestra todos os serviços da aplicação
```

## 🚀 Como Executar o Projeto

### Pré-requisitos
- Docker
- Docker Compose

### Passos para Execução

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/IsaacAiresTh/UniHack
   cd UniHack
   ```

2. **Construa e inicie os contêineres:**
   ```bash
   docker-compose up --build
   ```
   
   Este comando irá:
   - Construir a imagem Docker do back-end em Java
   - Construir as imagens dos desafios (como o `desafio-sqli`)
   - Iniciar os contêineres do back-end, PostgreSQL e desafios

3. **Acesse os serviços:**
   - 🌐 **API do back-end:** `http://localhost:8080`
   - 🗄️ **PostgreSQL:** `localhost:5433`
   - 🎯 **Desafio SQL Injection:** `http://localhost:3001`
   - 🎨 **Front-end (futuro):** `http://localhost:4200`

## 📚 Contribuindo

Este projeto é mantido por estudantes da UNICEPLAC. Contribuições são bem-vindas!

## 📄 Licença

Este projeto está licenciado sob a [MIT License](LICENSE).

---

<p align="center">
  Desenvolvido com ❤️ pelos estudantes da UNICEPLAC
</p>
