# 0004. Infraestrutura em free tiers

Data: 2026-10-22 · Status: Aceito

## Contexto
O projeto deve operar sem custo até ter receita. Os limites dos planos gratuitos mudam com
frequência e precisam ser revisados antes de cada adoção.

## Decisão
| Função | Serviço |
|---|---|
| Frontend | Vercel Hobby (fase de validação) |
| Backend | Oracle Cloud Always Free (VM ARM) com Docker e Caddy |
| Banco | Neon (Postgres serverless) |
| Arquivos | Cloudflare R2 |
| E-mail | Resend |
| CI/CD | GitHub Actions |
| Observabilidade | Sentry, Grafana Cloud, UptimeRobot |
| Pagamentos | Mercado Pago ou Asaas (apenas taxa por transação) |

## Consequências
- Custo zero na validação; o único custo opcional é o domínio.
- **O plano Hobby da Vercel não permite uso comercial.** Ao começar a cobrar, o frontend migra
  para Cloudflare (via OpenNext) ou para um plano pago. Isso será um novo ADR.
- O backend roda em container padrão (`backend/Dockerfile`), então trocar de provedor é barato.
- O Neon pode suspender o banco por inatividade, o que gera latência na primeira requisição.
