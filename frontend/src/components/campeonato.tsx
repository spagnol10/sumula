import Link from "next/link";
import type { Campeonato, LinhaArtilharia, LinhaClassificacao, Partida, Suspensao } from "@/lib/api";
import { InscreverJogadorForm, PlacarForm } from "./forms";

export function Cabecalho({ campeonato }: { campeonato: Campeonato }) {
  return (
    <header className="flex flex-col gap-1">
      <h1 className="text-3xl font-semibold tracking-tight">{campeonato.nome}</h1>
      <p className="text-sm text-zinc-500">
        {campeonato.times.length} times · {campeonato.idaEVolta ? "turno e returno" : "turno único"}
      </p>
    </header>
  );
}

export function TimesInscritos({ campeonato }: { campeonato: Campeonato }) {
  return (
    <section className="flex flex-col gap-3">
      <h2 className="text-xl font-semibold">Times inscritos</h2>
      {campeonato.times.length === 0 ? (
        <p className="text-zinc-500">Nenhum time inscrito ainda.</p>
      ) : (
        <ol className="flex list-decimal flex-col gap-1 pl-6">
          {campeonato.times.map((t) => (
            <li key={t.id}>{t.nome}</li>
          ))}
        </ol>
      )}
    </section>
  );
}

export function TabelaDeClassificacao({ linhas }: { linhas: LinhaClassificacao[] }) {
  const th = "px-2 py-2 text-right font-medium";
  const td = "px-2 py-2 text-right tabular-nums";
  return (
    <section className="flex flex-col gap-3">
      <h2 className="text-xl font-semibold">Classificação</h2>
      <div className="overflow-x-auto rounded-xl border border-zinc-200 dark:border-zinc-800">
        <table className="w-full text-sm">
          <thead className="bg-zinc-50 text-zinc-500 dark:bg-zinc-900">
            <tr>
              <th className="px-2 py-2 text-left font-medium">#</th>
              <th className="px-2 py-2 text-left font-medium">Time</th>
              <th className={th} title="Pontos">P</th>
              <th className={th} title="Jogos">J</th>
              <th className={th} title="Vitórias">V</th>
              <th className={th} title="Empates">E</th>
              <th className={th} title="Derrotas">D</th>
              <th className={th} title="Saldo de gols">SG</th>
              <th className={th} title="Gols pró">GP</th>
              <th className={th} title="Aproveitamento">%</th>
            </tr>
          </thead>
          <tbody>
            {linhas.map((l) => (
              <tr key={l.timeId} className="border-t border-zinc-200 dark:border-zinc-800">
                <td className="px-2 py-2 tabular-nums text-zinc-500">{l.posicao}</td>
                <td className="px-2 py-2 font-medium">{l.time}</td>
                <td className={`${td} font-semibold`}>{l.pontos}</td>
                <td className={td}>{l.jogos}</td>
                <td className={td}>{l.vitorias}</td>
                <td className={td}>{l.empates}</td>
                <td className={td}>{l.derrotas}</td>
                <td className={td}>{l.saldo}</td>
                <td className={td}>{l.golsPro}</td>
                <td className={td}>{l.aproveitamento}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}

const badge = {
  AGENDADA: null,
  EM_ANDAMENTO: (
    <span className="rounded bg-red-600 px-1.5 py-0.5 text-[10px] font-bold uppercase tracking-wide text-white">Ao vivo</span>
  ),
  ENCERRADA: <span className="text-[10px] font-medium uppercase tracking-wide text-zinc-400">Encerrado</span>,
} as const;

function Gols({ partida, timeId, alinhamento }: { partida: Partida; timeId: string; alinhamento: string }) {
  const gols = partida.eventos.filter((e) => e.tipo === "GOL" && e.timeId === timeId);
  if (gols.length === 0) return null;
  return (
    <ul className={`text-xs text-zinc-500 ${alinhamento}`}>
      {gols.map((g) => (
        <li key={g.id}>
          ⚽ {g.jogador ?? "Gol"}
          {g.minuto !== null && ` ${g.minuto}'`}
        </li>
      ))}
    </ul>
  );
}

export function Placar({ partida }: { partida: Partida }) {
  const temPlacar = partida.golsMandante !== null;
  return (
    <div className="flex flex-col gap-1">
      <div className="grid grid-cols-[1fr_auto_1fr] items-center gap-3">
        <span className="truncate text-right">{partida.mandante.nome}</span>
        <span className="min-w-14 rounded-md bg-zinc-100 px-2 py-1 text-center font-semibold tabular-nums dark:bg-zinc-800">
          {temPlacar ? `${partida.golsMandante} × ${partida.golsVisitante}` : "×"}
        </span>
        <span className="truncate">{partida.visitante.nome}</span>
      </div>
      <div className="grid grid-cols-[1fr_auto_1fr] gap-3">
        <Gols partida={partida} timeId={partida.mandante.id} alinhamento="text-right" />
        <div className="min-w-14 text-center">{badge[partida.status]}</div>
        <Gols partida={partida} timeId={partida.visitante.id} alinhamento="" />
      </div>
    </div>
  );
}

/** Rodadas com os jogos; com `campeonatoId` vira modo organizador (placar e link da súmula). */
export function Rodadas({ partidas, campeonatoId }: { partidas: Partida[]; campeonatoId?: string }) {
  const porRodada = Map.groupBy(partidas, (p) => p.rodada);
  return (
    <section className="flex flex-col gap-4">
      <h2 className="text-xl font-semibold">Jogos</h2>
      {[...porRodada].map(([rodada, jogos]) => (
        <div key={rodada} className="flex flex-col gap-3 rounded-xl border border-zinc-200 p-4 dark:border-zinc-800">
          <h3 className="text-sm font-medium text-zinc-500">{rodada}ª rodada</h3>
          {jogos.map((p) =>
            campeonatoId ? (
              <div key={p.id} className="flex flex-col gap-1">
                {p.placarPelaSumula ? (
                  <Placar partida={p} />
                ) : (
                  <PlacarForm
                    campeonatoId={campeonatoId}
                    partidaId={p.id}
                    mandante={p.mandante.nome}
                    visitante={p.visitante.nome}
                    golsMandante={p.golsMandante}
                    golsVisitante={p.golsVisitante}
                  />
                )}
                <Link
                  href={`/campeonatos/${campeonatoId}/partidas/${p.id}`}
                  className="self-center text-xs font-medium text-emerald-700 hover:underline dark:text-emerald-400"
                >
                  {p.status === "AGENDADA" ? "Abrir súmula" : "Súmula"}
                </Link>
              </div>
            ) : (
              <Placar key={p.id} partida={p} />
            ),
          )}
        </div>
      ))}
    </section>
  );
}

export function TabelaDeArtilharia({ linhas }: { linhas: LinhaArtilharia[] }) {
  if (linhas.length === 0) return null;
  return (
    <section className="flex flex-col gap-3">
      <h2 className="text-xl font-semibold">Artilharia</h2>
      <ol className="flex flex-col divide-y divide-zinc-200 rounded-xl border border-zinc-200 dark:divide-zinc-800 dark:border-zinc-800">
        {linhas.map((l) => (
          <li key={l.jogadorId} className="flex items-center gap-3 px-3 py-2 text-sm">
            <span className="w-5 tabular-nums text-zinc-500">{l.posicao}</span>
            <span className="font-medium">{l.jogador}</span>
            <span className="mr-auto text-zinc-500">{l.time}</span>
            <span className="font-semibold tabular-nums">{l.gols}</span>
          </li>
        ))}
      </ol>
    </section>
  );
}

const motivos = { CARTAO_VERMELHO: "🟥 Cartão vermelho", ACUMULO_DE_AMARELOS: "🟨 3 amarelos" } as const;

export function ListaDeSuspensos({ suspensos }: { suspensos: Suspensao[] }) {
  if (suspensos.length === 0) return null;
  return (
    <section className="flex flex-col gap-3">
      <h2 className="text-xl font-semibold">Suspensos para o próximo jogo</h2>
      <ul className="flex flex-col gap-1 text-sm">
        {suspensos.map((s) => (
          <li key={s.jogadorId}>
            <span className="font-medium">{s.jogador}</span> <span className="text-zinc-500">({s.time})</span> ·{" "}
            {motivos[s.motivo]} na {s.rodadaDoCartao}ª rodada
          </li>
        ))}
      </ul>
    </section>
  );
}

/** Organizador: elenco de cada time, com cadastro de jogadores. */
export function TimesEJogadores({ campeonato }: { campeonato: Campeonato }) {
  if (campeonato.times.length === 0) return null;
  return (
    <section className="flex flex-col gap-3">
      <h2 className="text-xl font-semibold">Elencos</h2>
      {campeonato.times.map((t) => (
        <details key={t.id} className="rounded-xl border border-zinc-200 p-3 dark:border-zinc-800">
          <summary className="cursor-pointer font-medium">
            {t.nome} <span className="text-sm font-normal text-zinc-500">({t.jogadores.length} jogadores)</span>
          </summary>
          <div className="mt-3 flex flex-col gap-2">
            {t.jogadores.length > 0 && (
              <ul className="flex flex-col gap-1 text-sm">
                {t.jogadores.map((j) => (
                  <li key={j.id}>
                    {j.numero !== null && <span className="tabular-nums text-zinc-500">{j.numero} · </span>}
                    {j.nome}
                  </li>
                ))}
              </ul>
            )}
            <InscreverJogadorForm campeonatoId={campeonato.id} timeId={t.id} />
          </div>
        </details>
      ))}
    </section>
  );
}
