CREATE TABLE campeonatos
(
    id          UUID PRIMARY KEY,
    nome        VARCHAR(120)             NOT NULL,
    slug        VARCHAR(80)              NOT NULL UNIQUE,
    ida_e_volta BOOLEAN                  NOT NULL,
    status      VARCHAR(30)              NOT NULL,
    criado_em   TIMESTAMP WITH TIME ZONE NOT NULL,
    versao      BIGINT                   NOT NULL
);

CREATE TABLE times
(
    id            UUID PRIMARY KEY,
    campeonato_id UUID                     NOT NULL REFERENCES campeonatos (id) ON DELETE CASCADE,
    nome          VARCHAR(80)              NOT NULL,
    inscrito_em   TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX times_nome_por_campeonato_uk ON times (campeonato_id, lower(nome));

-- Sem FK para campeonatos/times: são de outro módulo (ADR 0002).
CREATE TABLE partidas
(
    id             UUID PRIMARY KEY,
    campeonato_id  UUID        NOT NULL,
    rodada         INT         NOT NULL CHECK (rodada > 0),
    mandante_id    UUID        NOT NULL,
    visitante_id   UUID        NOT NULL,
    gols_mandante  INT CHECK (gols_mandante >= 0),
    gols_visitante INT CHECK (gols_visitante >= 0),
    status         VARCHAR(30) NOT NULL,
    versao         BIGINT      NOT NULL,
    CHECK (mandante_id <> visitante_id)
);

CREATE INDEX partidas_campeonato_rodada_idx ON partidas (campeonato_id, rodada);
