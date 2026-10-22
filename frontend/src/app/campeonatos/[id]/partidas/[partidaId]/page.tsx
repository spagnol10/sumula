import Link from "next/link";
import { notFound } from "next/navigation";
import { Suspense } from "react";
import { AoVivo } from "@/components/ao-vivo";
import { Placar } from "@/components/campeonato";
import { DesfazerEvento, EncerrarPartida, IniciarPartida, LancamentosDoTime } from "@/components/forms";
import { api, ApiError, type Partida, type TipoDeEvento } from "@/lib/api";
import { chaveDoOrganizador } from "@/lib/organizador";

/** Súmula da partida: o organizador (ou mesário) lança gols e cartões pelo celular. */
export default function PaginaDaSumula({ params }: PageProps<"/campeonatos/[id]/partidas/[partidaId]">) {
  return (
    <main className="mx-auto flex w-full max-w-xl flex-1 flex-col gap-6 px-4 py-8">
      <Suspense fallback={<p className="text-zinc-500">Carregando súmula…</p>}>
        <Conteudo params={params} />
      </Suspense>
    </main>
  );
}

const icones: Record<TipoDeEvento, string> = { GOL: "⚽", CARTAO_AMARELO: "🟨", CARTAO_VERMELHO: "🟥" };

async function Conteudo({ params }: { params: Promise<{ id: string; partidaId: string }> }) {
  const { id, partidaId } = await params;
  const [partida, campeonato, chave] = await Promise.all([
    buscar(partidaId),
    api.buscarCampeonato(id),
    chaveDoOrganizador(id),
  ]);
  if (partida.campeonatoId !== id) notFound();

  const nomeDoTime = (timeId: string) =>
    timeId === partida.mandante.id ? partida.mandante.nome : partida.visitante.nome;
  const elenco = (timeId: string) => campeonato.times.find((t) => t.id === timeId)?.jogadores ?? [];
  const podeLancar = !!chave && partida.status !== "AGENDADA";

  return (
    <>
      <div className="flex items-center justify-between">
        <Link href={`/campeonatos/${id}`} className="text-sm text-emerald-700 hover:underline dark:text-emerald-400">
          ← {campeonato.nome}
        </Link>
        <AoVivo campeonatoId={id} />
      </div>
      <h1 className="text-sm font-medium text-zinc-500">Súmula · {partida.rodada}ª rodada</h1>
      <div className="text-lg">
        <Placar partida={partida} />
      </div>

      {!chave && <p className="text-sm text-zinc-500">Somente o organizador pode lançar a súmula.</p>}
      {chave && partida.status === "AGENDADA" && <IniciarPartida campeonatoId={id} partidaId={partidaId} />}
      {chave && !partida.placarPelaSumula && partida.status === "ENCERRADA" && (
        <p className="text-sm text-zinc-500">O placar foi lançado direto. Você ainda pode registrar cartões.</p>
      )}

      {podeLancar && (
        <div className="grid gap-3 sm:grid-cols-2">
          {[partida.mandante, partida.visitante].map((time) => (
            <LancamentosDoTime
              key={time.id}
              campeonatoId={id}
              partidaId={partidaId}
              time={time}
              jogadores={elenco(time.id)}
              aceitaGols={partida.placarPelaSumula}
            />
          ))}
        </div>
      )}

      {partida.eventos.length > 0 && (
        <section className="flex flex-col gap-2">
          <h2 className="font-semibold">Lances</h2>
          <ol className="flex flex-col divide-y divide-zinc-200 rounded-xl border border-zinc-200 dark:divide-zinc-800 dark:border-zinc-800">
            {[...partida.eventos].reverse().map((e) => (
              <li key={e.id} className="flex items-center gap-2 px-3 py-2 text-sm">
                <span>{icones[e.tipo]}</span>
                <span className="w-8 tabular-nums text-zinc-500">{e.minuto !== null ? `${e.minuto}'` : ""}</span>
                <span className="mr-auto">
                  {e.jogador ?? (e.tipo === "GOL" ? "Gol" : "Cartão")}{" "}
                  <span className="text-zinc-500">({nomeDoTime(e.timeId)})</span>
                </span>
                {chave && <DesfazerEvento campeonatoId={id} partidaId={partidaId} eventoId={e.id} />}
              </li>
            ))}
          </ol>
        </section>
      )}

      {chave && partida.status === "EM_ANDAMENTO" && <EncerrarPartida campeonatoId={id} partidaId={partidaId} />}
    </>
  );
}

async function buscar(partidaId: string): Promise<Partida> {
  try {
    return await api.buscarPartida(partidaId);
  } catch (e) {
    if (e instanceof ApiError && (e.status === 404 || e.status === 400)) notFound();
    throw e;
  }
}
