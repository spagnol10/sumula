# 0007. Tempo real com SSE como sinal de invalidação

Data: 2026-10-22 · Status: Aceito

## Contexto
A súmula ao vivo precisa que placar, artilharia e classificação atualizem na tela de quem
acompanha, sem recarregar a página. O backend roda em uma única VM gratuita (ADR 0004), e a
renderização das telas já existe no Next (Server Components).

## Decisão
- **SSE** (`GET /api/v1/campeonatos/{id}/ao-vivo`) em vez de WebSocket: o fluxo é só do servidor
  para o cliente, funciona sobre HTTP comum (proxies e CDN) e o `EventSource` reconecta sozinho.
- O servidor manda apenas um **sinal** (`event: partida`, `data: <id>`), nunca os dados. O cliente
  chama `router.refresh()` e o Next busca tudo pelo caminho normal, então não existe uma segunda
  forma de renderizar a tela no cliente.
- O sinal sai **depois do commit** (`TransactionSynchronization.afterCommit`), para ninguém ler um
  estado que ainda pode sofrer rollback. Como é efêmero, não passa pelo outbox (ADR 0005).
- As conexões ficam em memória, com heartbeat a cada 20s, timeout de 30 min e limite global de
  2.000 conexões para proteger a VM.
- O navegador conecta direto na API (`NEXT_PUBLIC_API_URL`, liberado por CORS), porque conexões
  longas não combinam com funções serverless do frontend.

## Consequências
- Simples e sem infraestrutura extra.
- **Só funciona com uma instância do backend.** Para escalar horizontalmente, os sinais precisam
  de um canal compartilhado (ex.: `LISTEN/NOTIFY` do Postgres), que será um novo ADR.
- Cada sinal gera um refresh da página para todos os espectadores. Rajadas são agrupadas no
  cliente (300 ms), e o custo é aceitável para o volume de um campeonato amador.
