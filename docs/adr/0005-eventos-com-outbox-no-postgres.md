# 0005. Eventos entre módulos com outbox no Postgres

Data: 2026-10-22 · Status: Aceito

## Contexto
Módulos precisam reagir a fatos de outros módulos (ex.: `partidas` encerra um jogo e
`estatisticas` recalcula a classificação) sem acoplamento direto e sem perder eventos se a
aplicação cair no meio do processamento. Um broker (Redis, RabbitMQ, Kafka) seria mais uma
peça de infraestrutura para operar.

## Decisão
Usar eventos de aplicação do Spring com o *Event Publication Registry* do Spring Modulith
persistido via JDBC na tabela `event_publication` (migration `V1`). Os eventos são gravados
na mesma transação da mudança de estado (outbox transacional) e reprocessados se falharem.

## Consequências
- Entrega garantida "pelo menos uma vez" sem infraestrutura extra; os listeners precisam ser idempotentes.
- O schema da tabela é versionado pelo Flyway, não pela inicialização automática do Modulith,
  então upgrades do Spring Modulith devem conferir mudanças de schema.
- Se surgir necessidade de consumidores externos ou alto volume, os eventos podem ser
  externalizados para um broker sem mudar os módulos.
