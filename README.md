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

Escritas (🔑) exigem o cabeçalho `X-Chave-Organizador`, devolvido na criação ([ADR 0006](docs/adr/0006-chave-de-organizador.md)).

| Método | Rota | O que faz |
|---|---|---|
| `POST` | `/api/v1/campeonatos` | Cria um campeonato e devolve a chave do organizador |
| `GET` | `/api/v1/campeonatos/{id}` · `/slug/{slug}` | Detalhes e times inscritos |
| `POST` 🔑 | `/api/v1/campeonatos/{id}/times` | Inscreve um time |
| `POST` 🔑 | `/api/v1/campeonatos/{id}/iniciar` | Fecha as inscrições e dispara a geração da tabela |
| `POST` 🔑 | `/api/v1/campeonatos/{id}/chave/verificacao` | Confere uma chave (204 ou 403) |
| `GET` | `/api/v1/campeonatos/{id}/partidas` | Tabela de jogos por rodada |
| `PUT` 🔑 | `/api/v1/partidas/{id}/resultado` | Lança ou corrige o placar |
| `POST` 🔑 | `/api/v1/campeonatos/{id}/times/{timeId}/jogadores` | Inscreve um jogador (número opcional) |
| `GET` | `/api/v1/campeonatos/{id}/classificacao` | Classificação com critérios de desempate |
| `GET` | `/api/v1/campeonatos/{id}/artilharia` | Artilheiros (conta jogos em andamento) |
| `GET` | `/api/v1/campeonatos/{id}/suspensoes` | Suspensos para o próximo jogo (vermelho ou 3 amarelos) |
| `GET` | `/api/v1/campeonatos/{id}/ao-vivo` | SSE: avisa quando uma partida muda ([ADR 0007](docs/adr/0007-tempo-real-com-sse.md)) |
| `GET` | `/api/v1/partidas/{id}` | Partida com a súmula |
| `POST` 🔑 | `/api/v1/partidas/{id}/inicio` · `/encerramento` | Inicia ou encerra a partida |
| `POST` 🔑 | `/api/v1/partidas/{id}/eventos` | Lança gol ou cartão; o placar é a contagem dos gols |
| `DELETE` 🔑 | `/api/v1/partidas/{id}/eventos/{eventoId}` | Desfaz um lance |

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
- [ ] **Fase 2, ao vivo:** ~~página pública~~, ~~súmula em tempo real (SSE)~~, ~~artilharia~~, ~~suspensões~~, previews para WhatsApp
- [ ] **Fase 3, monetização:** planos Free/Pro, inscrição paga via Pix, painel do organizador
- [ ] **Fase 4, crescimento:** artes automáticas, resumo da rodada com IA, rankings
