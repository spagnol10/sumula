import { notFound } from "next/navigation";
import { Suspense } from "react";
import { AtualizacaoAutomatica } from "@/components/atualizacao-automatica";
import { InscreverTimeForm, IniciarCampeonatoForm, PlacarForm } from "@/components/forms";
import { api, ApiError, type Campeonato, type LinhaClassificacao, type Partida } from "@/lib/api";

export default function PaginaDoCampeonato({ params }: PageProps<"/campeonatos/[id]">) {
  return (
    <main className="mx-auto flex w-full max-w-3xl flex-1 flex-col gap-8 px-4 py-10">
      <Suspense fallback={<p className="text-zinc-500">Carregando campeonato…</p>}>
        <ConteudoDoCampeonato params={params} />
      </Suspense>
    </main>
  );
}

async function ConteudoDoCampeonato({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  let campeonato: Campeonato;
  try {
    campeonato = await api.buscarCampeonato(id);
  } catch (e) {
    if (e instanceof ApiError && (e.status === 404 || e.status === 400)) notFound();
    throw e;
  }

  return (
    <>
      <header className="flex flex-col gap-1">
        <h1 className="text-3xl font-semibold tracking-tight">{campeonato.nome}</h1>
        <p className="text-sm text-zinc-500">
          {campeonato.times.length} times · {campeonato.idaEVolta ? "turno e returno" : "turno único"}
        </p>
      </header>
      {campeonato.status === "INSCRICOES_ABERTAS" ? (
        <Inscricoes campeonato={campeonato} />
      ) : (
        <EmAndamento campeonatoId={campeonato.id} />
      )}
    </>
  );
}

function Inscricoes({ campeonato }: { campeonato: Campeonato }) {
  return (
    <section className="flex flex-col gap-4">
      <div className="rounded-lg border border-amber-300 bg-amber-50 p-3 text-sm text-amber-900 dark:border-amber-800 dark:bg-amber-950 dark:text-amber-200">
        Guarde o link desta página: por enquanto é por ele que você administra o campeonato.
      </div>
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
      <InscreverTimeForm campeonatoId={campeonato.id} />
      <IniciarCampeonatoForm campeonatoId={campeonato.id} podeIniciar={campeonato.times.length >= 2} />
    </section>
  );
}

async function EmAndamento({ campeonatoId }: { campeonatoId: string }) {
  const [partidas, classificacao] = await Promise.all([
    api.listarPartidas(campeonatoId),
    api.classificacao(campeonatoId),
  ]);

  if (partidas.length === 0) {
    return (
      <p className="text-zinc-500">
        Gerando a tabela de jogos…
        <AtualizacaoAutomatica />
      </p>
    );
  }

  return (
    <>
      <TabelaDeClassificacao linhas={classificacao} />
      <Rodadas partidas={partidas} />
    </>
  );
}

function TabelaDeClassificacao({ linhas }: { linhas: LinhaClassificacao[] }) {
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

function Rodadas({ partidas }: { partidas: Partida[] }) {
  const porRodada = Map.groupBy(partidas, (p) => p.rodada);
  return (
    <section className="flex flex-col gap-4">
      <h2 className="text-xl font-semibold">Jogos</h2>
      {[...porRodada].map(([rodada, jogos]) => (
        <div key={rodada} className="flex flex-col gap-2 rounded-xl border border-zinc-200 p-4 dark:border-zinc-800">
          <h3 className="text-sm font-medium text-zinc-500">{rodada}ª rodada</h3>
          {jogos.map((p) => (
            <PlacarForm
              key={p.id}
              partidaId={p.id}
              mandante={p.mandante.nome}
              visitante={p.visitante.nome}
              golsMandante={p.golsMandante}
              golsVisitante={p.golsVisitante}
            />
          ))}
        </div>
      ))}
    </section>
  );
}
