CREATE TABLE jogadores
(
    id          UUID PRIMARY KEY,
    time_id     UUID                     NOT NULL REFERENCES times (id) ON DELETE CASCADE,
    nome        VARCHAR(80)              NOT NULL,
    numero      INT CHECK (numero BETWEEN 0 AND 999),
    inscrito_em TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX jogadores_time_idx ON jogadores (time_id);
CREATE UNIQUE INDEX jogadores_numero_por_time_uk ON jogadores (time_id, numero) WHERE numero IS NOT NULL;

ALTER TABLE partidas ADD COLUMN placar_pela_sumula BOOLEAN NOT NULL DEFAULT FALSE;

-- time_id/jogador_id sem FK: pertencem ao módulo de campeonatos (ADR 0002).
CREATE TABLE eventos_partida
(
    id            UUID PRIMARY KEY,
    partida_id    UUID                     NOT NULL REFERENCES partidas (id) ON DELETE CASCADE,
    tipo          VARCHAR(30)              NOT NULL,
    time_id       UUID                     NOT NULL,
    jogador_id    UUID,
    minuto        INT CHECK (minuto BETWEEN 0 AND 200),
    registrado_em TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX eventos_partida_partida_idx ON eventos_partida (partida_id);
