# 0003. Monorepo com backend e frontend

Data: 2026-10-22 · Status: Aceito

## Contexto
Backend (Java/Spring) e frontend (Next.js) evoluem juntos: uma feature normalmente toca os dois
e o contrato da API entre eles.

## Decisão
Um único repositório com `backend/` e `frontend/`, cada um com seu próprio build.
O CI usa filtro de caminhos para rodar só o job afetado.

## Consequências
- Uma feature = um PR, com contrato e consumo revisados juntos.
- O client TypeScript poderá ser gerado a partir do OpenAPI do backend no mesmo repositório.
- Ferramentas de cada lado (Maven, npm) continuam independentes; não há orquestrador de monorepo.
