# 0006. Chave de organizador (provisória até o login)

Data: 2026-10-22 · Status: Aceito

## Contexto
A Fase 2 publica uma página aberta do campeonato, que expõe ids de campeonato e de partidas.
Até aqui, conhecer um id bastava para alterar placares. O login com Google (OAuth) depende
de credenciais que ainda não existem, e o produto não pode publicar páginas abertas sem
proteger as escritas.

## Decisão
- Na criação, o backend gera uma chave aleatória de 256 bits, devolvida **uma única vez**.
  Só o hash SHA-256 é persistido, e a comparação é feita em tempo constante.
- Toda escrita exige o cabeçalho `X-Chave-Organizador`. Os outros módulos usam
  `Campeonatos.exigirOrganizador(...)`. Leituras continuam abertas.
- O Next (BFF) guarda a chave num cookie `httpOnly`, `SameSite=Lax`, por campeonato, e a envia
  à API pelo servidor. O JavaScript do navegador nunca a enxerga.
- Para usar outro aparelho, o organizador copia um link de acesso (`/campeonatos/{id}/acesso?chave=…`).
  O Route Handler valida a chave, grava o cookie e redireciona, tirando a chave da barra de endereços.

## Consequências
- Ids deixam de ser segredo, e a página pública pode existir com segurança.
- Perder a chave significa perder o acesso, e não há recuperação. Aceitável enquanto não houver contas.
- A chave aparece na query string do link de acesso e pode ficar no histórico do navegador e em logs
  de proxy. Mitigação: o redirecionamento imediato.
- Quando o login entrar, o campeonato ganha um dono (usuário), e a chave vira um mecanismo
  de convite de co-organizadores ou é removida (novo ADR).
