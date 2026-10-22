"use client";

import { useActionState } from "react";
import {
  criarCampeonato,
  encerrarPartida,
  iniciarCampeonato,
  iniciarPartida,
  inscreverJogador,
  inscreverTime,
  registrarEvento,
  registrarResultado,
  removerEvento,
  type EstadoForm,
} from "@/app/actions";

const inicial: EstadoForm = {};

const input =
  "w-full rounded-lg border border-zinc-300 bg-white px-3 py-2 text-base outline-none focus:border-emerald-600 focus:ring-2 focus:ring-emerald-600/20 dark:border-zinc-700 dark:bg-zinc-900";
const botao =
  "rounded-lg bg-emerald-600 px-4 py-2 font-medium text-white hover:bg-emerald-700 disabled:opacity-60";

function Erro({ estado }: { estado: EstadoForm }) {
  return (
    <p aria-live="polite" className="min-h-5 text-sm text-red-600 dark:text-red-400">
      {estado.erro}
    </p>
  );
}

export function NovoCampeonatoForm() {
  const [estado, action, pendente] = useActionState(criarCampeonato, inicial);
  return (
    <form action={action} className="flex flex-col gap-4">
      <label className="flex flex-col gap-1">
        <span className="text-sm font-medium">Nome do campeonato</span>
        <input name="nome" required maxLength={120} placeholder="Copa da Vila 2026" className={input} />
      </label>
      <label className="flex items-center gap-2">
        <input type="checkbox" name="idaEVolta" className="size-4 accent-emerald-600" />
        <span>Turno e returno (ida e volta)</span>
      </label>
      <Erro estado={estado} />
      <button disabled={pendente} className={botao}>
        {pendente ? "Criando…" : "Criar campeonato"}
      </button>
    </form>
  );
}

export function InscreverTimeForm({ campeonatoId }: { campeonatoId: string }) {
  const [estado, action, pendente] = useActionState(inscreverTime.bind(null, campeonatoId), inicial);
  return (
    <form action={action} className="flex flex-col gap-1">
      <div className="flex gap-2">
        <input name="nome" required maxLength={80} placeholder="Nome do time" aria-label="Nome do time" className={input} />
        <button disabled={pendente} className={`${botao} shrink-0`}>
          Inscrever
        </button>
      </div>
      <Erro estado={estado} />
    </form>
  );
}

export function IniciarCampeonatoForm({ campeonatoId, podeIniciar }: { campeonatoId: string; podeIniciar: boolean }) {
  const [estado, action, pendente] = useActionState(iniciarCampeonato.bind(null, campeonatoId), inicial);
  return (
    <form action={action} className="flex flex-col gap-1">
      <button disabled={pendente || !podeIniciar} className={botao}>
        {pendente ? "Gerando tabela…" : "Encerrar inscrições e gerar tabela"}
      </button>
      {!podeIniciar && <p className="text-sm text-zinc-500">Inscreva pelo menos 2 times para começar.</p>}
      <Erro estado={estado} />
    </form>
  );
}

const golsInput =
  "w-12 rounded-md border border-zinc-300 bg-white px-1 py-1 text-center tabular-nums dark:border-zinc-700 dark:bg-zinc-900";

export function PlacarForm({
  campeonatoId,
  partidaId,
  mandante,
  visitante,
  golsMandante,
  golsVisitante,
}: {
  campeonatoId: string;
  partidaId: string;
  mandante: string;
  visitante: string;
  golsMandante: number | null;
  golsVisitante: number | null;
}) {
  const [estado, action, pendente] = useActionState(registrarResultado.bind(null, campeonatoId, partidaId), inicial);
  return (
    <form action={action} className="flex flex-col gap-1">
      <div className="grid grid-cols-[1fr_auto_auto_auto_1fr_auto] items-center gap-2">
        <span className="truncate text-right">{mandante}</span>
        <input name="golsMandante" type="number" min={0} max={99} required defaultValue={golsMandante ?? ""} aria-label={`Gols ${mandante}`} className={golsInput} />
        <span className="text-zinc-400">×</span>
        <input name="golsVisitante" type="number" min={0} max={99} required defaultValue={golsVisitante ?? ""} aria-label={`Gols ${visitante}`} className={golsInput} />
        <span className="truncate">{visitante}</span>
        <button disabled={pendente} className="rounded-md px-2 py-1 text-sm font-medium text-emerald-700 hover:bg-emerald-50 disabled:opacity-60 dark:text-emerald-400 dark:hover:bg-emerald-950">
          {golsMandante === null ? "Salvar" : "Corrigir"}
        </button>
      </div>
      {estado.erro && <Erro estado={estado} />}
    </form>
  );
}

export function InscreverJogadorForm({ campeonatoId, timeId }: { campeonatoId: string; timeId: string }) {
  const [estado, action, pendente] = useActionState(inscreverJogador.bind(null, campeonatoId, timeId), inicial);
  return (
    <form action={action} className="flex flex-col gap-1">
      <div className="flex gap-2">
        <input name="nome" required maxLength={80} placeholder="Nome do jogador" aria-label="Nome do jogador" className={input} />
        <input name="numero" type="number" min={0} max={999} placeholder="Nº" aria-label="Número da camisa" className={`${input} w-20 shrink-0`} />
        <button disabled={pendente} className={`${botao} shrink-0`}>
          Adicionar
        </button>
      </div>
      <Erro estado={estado} />
    </form>
  );
}

/** Botão que dispara uma action sem campos (iniciar, encerrar, desfazer). */
export function BotaoDeAcao({
  acao,
  rotulo,
  pendenteRotulo,
  variante = "primario",
}: {
  acao: () => Promise<EstadoForm>;
  rotulo: string;
  pendenteRotulo?: string;
  variante?: "primario" | "discreto";
}) {
  const [estado, action, pendente] = useActionState(acao, inicial);
  const estilo =
    variante === "primario"
      ? botao
      : "rounded-md px-2 py-1 text-xs font-medium text-zinc-500 hover:bg-zinc-100 hover:text-red-600 dark:hover:bg-zinc-800";
  return (
    <form action={action} className="flex flex-col gap-1">
      <button disabled={pendente} className={estilo}>
        {pendente && pendenteRotulo ? pendenteRotulo : rotulo}
      </button>
      {estado.erro && <Erro estado={estado} />}
    </form>
  );
}

export function IniciarPartida({ campeonatoId, partidaId }: { campeonatoId: string; partidaId: string }) {
  return <BotaoDeAcao acao={iniciarPartida.bind(null, campeonatoId, partidaId)} rotulo="Iniciar partida" pendenteRotulo="Iniciando…" />;
}

export function EncerrarPartida({ campeonatoId, partidaId }: { campeonatoId: string; partidaId: string }) {
  return <BotaoDeAcao acao={encerrarPartida.bind(null, campeonatoId, partidaId)} rotulo="Encerrar partida" pendenteRotulo="Encerrando…" />;
}

export function DesfazerEvento({ campeonatoId, partidaId, eventoId }: { campeonatoId: string; partidaId: string; eventoId: string }) {
  return (
    <BotaoDeAcao acao={removerEvento.bind(null, campeonatoId, partidaId, eventoId)} rotulo="Desfazer" variante="discreto" />
  );
}

/** Lançamentos de um time na súmula: o botão clicado define o tipo do evento. */
export function LancamentosDoTime({
  campeonatoId,
  partidaId,
  time,
  jogadores,
  aceitaGols,
}: {
  campeonatoId: string;
  partidaId: string;
  time: { id: string; nome: string };
  jogadores: { id: string; nome: string; numero: number | null }[];
  aceitaGols: boolean;
}) {
  const [estado, action, pendente] = useActionState(
    registrarEvento.bind(null, campeonatoId, partidaId, time.id),
    inicial,
  );
  const acao = "flex-1 rounded-lg border border-zinc-300 px-2 py-2 text-sm font-medium hover:bg-zinc-100 disabled:opacity-60 dark:border-zinc-700 dark:hover:bg-zinc-800";
  return (
    <form action={action} aria-label={`Lançamentos do ${time.nome}`} className="flex flex-col gap-2 rounded-xl border border-zinc-200 p-3 dark:border-zinc-800">
      <h3 className="font-semibold">{time.nome}</h3>
      <div className="flex gap-2">
        <select name="jogadorId" aria-label={`Jogador do ${time.nome}`} className={input} defaultValue="">
          <option value="">Jogador (opcional no gol)</option>
          {jogadores.map((j) => (
            <option key={j.id} value={j.id}>
              {j.numero !== null ? `${j.numero} · ` : ""}
              {j.nome}
            </option>
          ))}
        </select>
        <input name="minuto" type="number" min={0} max={200} placeholder="Min" aria-label="Minuto" className={`${input} w-20 shrink-0`} />
      </div>
      <div className="flex gap-2">
        {aceitaGols && (
          <button name="tipo" value="GOL" disabled={pendente} className={acao}>
            ⚽ Gol
          </button>
        )}
        <button name="tipo" value="CARTAO_AMARELO" disabled={pendente} className={acao}>
          🟨 Amarelo
        </button>
        <button name="tipo" value="CARTAO_VERMELHO" disabled={pendente} className={acao}>
          🟥 Vermelho
        </button>
      </div>
      {estado.erro && <Erro estado={estado} />}
    </form>
  );
}
