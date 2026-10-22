# Súmula ⚽

Plataforma para organizar campeonatos amadores (várzea, society, futsal, ligas de empresa):
tabela gerada automaticamente, súmula ao vivo pelo celular, classificação, artilharia e
suspensões calculadas sozinhas, e uma página pública para jogadores e torcida acompanharem.

> 🚧 Em construção: Fase 0 (fundação).

## Arquitetura

```
[Next.js] ──HTTPS/JWT──> [Spring Boot · monolito modular] ──> [PostgreSQL]
    ▲                          │  SSE (placar ao vivo)
    └──────────────────────────┘  ├──> Cloudflare R2 (escudos, artes)
                                  └──> Resend (e-mails)
```

- **Backend:** Java 21, Spring Boot 4, Spring Modulith, Flyway, PostgreSQL, virtual threads
- **Frontend:** Next.js 16 (App Router), React 19, TypeScript, Tailwind CSS 4
- **Qualidade:** testes de fronteira entre módulos, Testcontainers, E2E com Playwright, CI no GitHub Actions, Dependabot

As decisões de arquitetura e seus porquês estão em [`docs/adr`](docs/adr).

## API

Documentação interativa em `http://localhost:8080/api/docs/ui` (OpenAPI em `/api/docs`).

| Método | Rota | O que faz |
|---|---|---|
| `POST` | `/api/v1/campeonatos` | Cria um campeonato (pontos corridos, turno único ou ida e volta) |
| `GET` | `/api/v1/campeonatos/{id}` | Detalhes e times inscritos |
| `POST` | `/api/v1/campeonatos/{id}/times` | Inscreve um time |
| `POST` | `/api/v1/campeonatos/{id}/iniciar` | Fecha as inscrições e dispara a geração da tabela |
| `GET` | `/api/v1/campeonatos/{id}/partidas` | Tabela de jogos por rodada |
| `PUT` | `/api/v1/partidas/{id}/resultado` | Lança ou corrige o placar |
| `GET` | `/api/v1/campeonatos/{id}/classificacao` | Classificação com critérios de desempate |

A tabela usa o método do círculo (Berger) com mando equilibrado e o mínimo teórico de jogos
seguidos com o mesmo mando. Ver `GeradorDeTabela` e seus testes de propriedade.

## Rodando localmente

Pré-requisitos: Java 21, Node 22 e Docker.

```bash
docker compose up -d            # Postgres local

cd backend && ./mvnw spring-boot:run
# API em http://localhost:8080 · saúde em /actuator/health

cd frontend && cp .env.example .env.local && npm install && npm run dev
# Web em http://localhost:3000
```

Testes:
- Backend: `cd backend && ./mvnw verify` (precisa do Docker para o Testcontainers)
- E2E: com backend e frontend rodando, `cd frontend && npm run e2e` (Playwright, viewport de celular)

O CI roda os três: backend com Postgres via Testcontainers, frontend (lint, tipos, build) e E2E
com Postgres de serviço, API e Next de produção.

## Roadmap

- [ ] **Fase 0, fundação:** monorepo, CI/CD, ADRs, deploy das duas pontas
- [ ] **Fase 1, MVP:** login, campeonatos, times e jogadores, geração de tabela, resultados, classificação pública
- [ ] **Fase 2, ao vivo:** súmula em tempo real (SSE), artilharia, suspensões, previews para WhatsApp
- [ ] **Fase 3, monetização:** planos Free/Pro, inscrição paga via Pix, painel do organizador
- [ ] **Fase 4, crescimento:** artes automáticas, resumo da rodada com IA, rankings
