import Link from "next/link";
import { notFound } from "next/navigation";
import { Suspense } from "react";
import { AtualizacaoAutomatica } from "@/components/atualizacao-automatica";
import { Cabecalho, Rodadas, TabelaDeClassificacao, TimesInscritos } from "@/components/campeonato";
import { CopiarLink } from "@/components/copiar-link";
import { InscreverTimeForm, IniciarCampeonatoForm } from "@/components/forms";
import { api, ApiError, type Campeonato } from "@/lib/api";
import { chaveDoOrganizador } from "@/lib/organizador";

/** Área do organizador. Quem não tem a chave vê só um atalho para a página pública. */
export default function PaginaDoOrganizador({ params }: PageProps<"/campeonatos/[id]">) {
  return (
    <main className="mx-auto flex w-full max-w-3xl flex-1 flex-col gap-8 px-4 py-10">
      <Suspense fallback={<p className="text-zinc-500">Carregando campeonato…</p>}>
        <Conteudo params={params} />
      </Suspense>
    </main>
  );
}

async function Conteudo({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  const [campeonato, chave] = await Promise.all([buscar(id), chaveDoOrganizador(id)]);
  const paginaPublica = `/c/${campeonato.slug}`;

  if (!chave) {
    return (
      <>
        <Cabecalho campeonato={campeonato} />
        <p className="text-zinc-600 dark:text-zinc-400">
          Esta é a área do organizador. Para administrar, abra o link de acesso que o organizador recebeu.
        </p>
        <Link href={paginaPublica} className="self-start text-emerald-700 underline dark:text-emerald-400">
          Ver a página pública do campeonato
        </Link>
      </>
    );
  }

  return (
    <>
      <Cabecalho campeonato={campeonato} />
      <section className="flex flex-col gap-3 rounded-xl border border-zinc-200 p-4 dark:border-zinc-800">
        <div className="flex flex-wrap items-center gap-2">
          <Link href={paginaPublica} className="mr-auto font-medium text-emerald-700 underline dark:text-emerald-400">
            Página pública
          </Link>
          <CopiarLink caminho={paginaPublica} rotulo="Copiar link para os times" />
          <CopiarLink
            caminho={`/campeonatos/${id}/acesso?chave=${encodeURIComponent(chave)}`}
            rotulo="Copiar meu link de acesso"
          />
        </div>
        <p className="text-xs text-zinc-500">
          O link de acesso dá controle total do campeonato. Guarde-o para entrar em outro aparelho e não compartilhe.
        </p>
      </section>
      {campeonato.status === "INSCRICOES_ABERTAS" ? (
        <Inscricoes campeonato={campeonato} />
      ) : (
        <EmAndamento campeonatoId={id} />
      )}
    </>
  );
}

async function buscar(id: string): Promise<Campeonato> {
  try {
    return await api.buscarCampeonato(id);
  } catch (e) {
    if (e instanceof ApiError && (e.status === 404 || e.status === 400)) notFound();
    throw e;
  }
}

function Inscricoes({ campeonato }: { campeonato: Campeonato }) {
  return (
    <>
      <TimesInscritos campeonato={campeonato} />
      <InscreverTimeForm campeonatoId={campeonato.id} />
      <IniciarCampeonatoForm campeonatoId={campeonato.id} podeIniciar={campeonato.times.length >= 2} />
    </>
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
      <Rodadas partidas={partidas} campeonatoId={campeonatoId} />
    </>
  );
}
