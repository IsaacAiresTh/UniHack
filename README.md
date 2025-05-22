# UniHack

**UniHack** é uma plataforma de *Capture The Flag* (CTF) gamificada, desenvolvida com o objetivo de fomentar a cultura de segurança da informação entre os alunos da **UNICEPLAC**. Através de desafios práticos, rankings, conquistas e níveis, a plataforma incentiva o aprendizado contínuo em um ambiente competitivo e colaborativo.

## 🎯 Objetivos

- Promover o estudo de cybersecurity por meio de desafios técnicos e práticos.
- Estimular a formação de equipes e o trabalho colaborativo entre estudantes.
- Integrar uma plataforma de CTF à grade extracurricular da instituição.
- Oferecer uma ferramenta escalável para organização de eventos internos de segurança.

## ⚙️ Tecnologias Utilizadas

- **Back-end:** Java + Spring Boot  
- **Front-end:** Angular 19
- **Banco de Dados:** PostgreSQL  
- **Containerização:** Docker

## 🧩 Funcionalidades Previstas

- Sistema de autenticação de usuários e perfis personalizados.
- Painel administrativo para criação e gerenciamento de desafios.
- Sistema de pontuação com ranking individual e por equipe.
- Conquistas e níveis baseados no desempenho dos participantes.
- Página com histórico de competições e estatísticas.

## 🚀 Como Executar o Projeto

> Requisitos: Docker e Docker Compose instalados

```bash
git clone https://github.com/Isaac-code-maker/UniHack
cd UniHack
docker-compose up --build
```

> A interface estará disponível em: `http://localhost:3000`  
> A API estará disponível em: `http://localhost:8080`

## 🛠️ Estrutura do Projeto

```
unihack/
├── backend/        # Projeto Spring Boot
├── frontend/       # Projeto Angular
├── docker/         # Configurações de containerização
└── docs/           # Documentação técnica
```

## 📚 Contribuindo

Este projeto é mantido por estudantes da UNICEPLAC

## 📄 Licença

Este projeto está licenciado sob a [MIT License](LICENSE).

