-- Hash SHA-256 (hex) da chave de organizador (ADR 0006).
-- Campeonatos criados antes desta versão ficam sem chave válida, ou seja, somente leitura.
ALTER TABLE campeonatos ADD COLUMN chave_organizador_hash VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE campeonatos ALTER COLUMN chave_organizador_hash DROP DEFAULT;
