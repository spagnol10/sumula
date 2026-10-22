import type { Campeonato, LinhaClassificacao, Partida } from "@/lib/api";
import { PlacarForm } from "./forms";

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

function Placar({ partida }: { partida: Partida }) {
  const encerrada = partida.status === "ENCERRADA";
  return (
    <div className="grid grid-cols-[1fr_auto_1fr] items-center gap-3">
      <span className="truncate text-right">{partida.mandante.nome}</span>
      <span className="min-w-14 rounded-md bg-zinc-100 px-2 py-1 text-center font-semibold tabular-nums dark:bg-zinc-800">
        {encerrada ? `${partida.golsMandante} × ${partida.golsVisitante}` : "×"}
      </span>
      <span className="truncate">{partida.visitante.nome}</span>
    </div>
  );
}

/** Rodadas com os jogos; com `campeonatoId` vira modo organizador (placar editável). */
export function Rodadas({ partidas, campeonatoId }: { partidas: Partida[]; campeonatoId?: string }) {
  const porRodada = Map.groupBy(partidas, (p) => p.rodada);
  return (
    <section className="flex flex-col gap-4">
      <h2 className="text-xl font-semibold">Jogos</h2>
      {[...porRodada].map(([rodada, jogos]) => (
        <div key={rodada} className="flex flex-col gap-2 rounded-xl border border-zinc-200 p-4 dark:border-zinc-800">
          <h3 className="text-sm font-medium text-zinc-500">{rodada}ª rodada</h3>
          {jogos.map((p) =>
            campeonatoId ? (
              <PlacarForm
                key={p.id}
                campeonatoId={campeonatoId}
                partidaId={p.id}
                mandante={p.mandante.nome}
                visitante={p.visitante.nome}
                golsMandante={p.golsMandante}
                golsVisitante={p.golsVisitante}
              />
            ) : (
              <Placar key={p.id} partida={p} />
            ),
          )}
        </div>
      ))}
    </section>
  );
}
