import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { Suspense } from "react";
import { AoVivo } from "@/components/ao-vivo";
import {
  Cabecalho,
  ListaDeSuspensos,
  Rodadas,
  TabelaDeArtilharia,
  TabelaDeClassificacao,
  TimesInscritos,
} from "@/components/campeonato";
import { api, ApiError, type Campeonato } from "@/lib/api";

export async function generateMetadata({ params }: PageProps<"/c/[slug]">): Promise<Metadata> {
  const { slug } = await params;
  try {
    const campeonato = await api.buscarPorSlug(slug);
    const descricao =
      campeonato.status === "INSCRICOES_ABERTAS"
        ? `Inscrições abertas · ${campeonato.times.length} times inscritos`
        : `Classificação, jogos e artilharia ao vivo · ${campeonato.times.length} times`;
    return {
      title: `${campeonato.nome} · Súmula`,
      description: descricao,
      openGraph: { title: campeonato.nome, description: descricao, siteName: "Súmula", type: "website" },
    };
  } catch {
    return { title: "Campeonato · Súmula" };
  }
}

/** Página pública do campeonato: o link que o organizador manda para os times. Só leitura. */
export default function PaginaPublica({ params }: PageProps<"/c/[slug]">) {
  return (
    <main className="mx-auto flex w-full max-w-3xl flex-1 flex-col gap-8 px-4 py-10">
      <Suspense fallback={<p className="text-zinc-500">Carregando campeonato…</p>}>
        <Conteudo params={params} />
      </Suspense>
    </main>
  );
}

async function Conteudo({ params }: { params: Promise<{ slug: string }> }) {
  const { slug } = await params;
  let campeonato: Campeonato;
  try {
    campeonato = await api.buscarPorSlug(slug);
  } catch (e) {
    if (e instanceof ApiError && e.status === 404) notFound();
    throw e;
  }

  if (campeonato.status === "INSCRICOES_ABERTAS") {
    return (
      <>
        <Cabecalho campeonato={campeonato} />
        <p className="text-zinc-600 dark:text-zinc-400">Inscrições abertas. A tabela sai quando o organizador fechar as inscrições.</p>
        <TimesInscritos campeonato={campeonato} />
      </>
    );
  }

  const [partidas, classificacao, artilharia, suspensos] = await Promise.all([
    api.listarPartidas(campeonato.id),
    api.classificacao(campeonato.id),
    api.artilharia(campeonato.id),
    api.suspensoes(campeonato.id),
  ]);
  return (
    <>
      <Cabecalho campeonato={campeonato} />
      <AoVivo campeonatoId={campeonato.id} />
      <TabelaDeClassificacao linhas={classificacao} />
      <Rodadas partidas={partidas} />
      <TabelaDeArtilharia linhas={artilharia} />
      <ListaDeSuspensos suspensos={suspensos} />
    </>
  );
}
