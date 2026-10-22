# 0002. Monolito modular com Spring Modulith

Data: 2026-10-22 · Status: Aceito

## Contexto
O produto ainda não tem usuários. Microsserviços trariam custo operacional (vários deploys,
rede entre serviços, consistência distribuída) sem benefício nesta fase, e a infraestrutura
precisa caber em free tiers (ADR 0004). Ao mesmo tempo, queremos fronteiras claras para que o
domínio não vire uma "bola de lama".

## Decisão
Um único backend Spring Boot organizado em módulos de domínio, um pacote direto de
`br.com.sumula` por módulo: `campeonatos`, `partidas`, `estatisticas`, `identidade`,
`notificacoes`, `cobranca`.

- Cada módulo expõe apenas sua API pública (pacote raiz do módulo); subpacotes são internos.
- Módulos se comunicam por chamadas à API pública ou por eventos de domínio (ADR 0005).
- `ModularityTests` roda `ApplicationModules.verify()` no CI e quebra o build se uma fronteira for violada.

## Consequências
- Um deploy, um banco, transações locais simples.
- Se um módulo precisar escalar separadamente, ele já tem fronteira e eventos definidos para ser extraído.
- Exige disciplina: a verificação automatizada substitui a separação física.
