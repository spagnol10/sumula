import Link from "next/link";

export default function Home() {
  return (
    <main className="mx-auto flex w-full max-w-2xl flex-1 flex-col justify-center gap-8 px-4 py-16">
      <div className="flex flex-col gap-4">
        <h1 className="text-4xl font-semibold tracking-tight sm:text-5xl">Súmula</h1>
        <p className="text-lg text-zinc-600 dark:text-zinc-400">
          Organize seu campeonato amador sem planilha: a tabela é gerada sozinha, os placares entram pelo
          celular e a classificação se atualiza na hora.
        </p>
      </div>
      <ul className="grid gap-3 text-zinc-700 sm:grid-cols-3 dark:text-zinc-300">
        <li className="rounded-xl border border-zinc-200 p-4 dark:border-zinc-800">📅 Tabela automática com mando equilibrado</li>
        <li className="rounded-xl border border-zinc-200 p-4 dark:border-zinc-800">⚽ Placar lançado em segundos</li>
        <li className="rounded-xl border border-zinc-200 p-4 dark:border-zinc-800">🏆 Classificação com critérios de desempate</li>
      </ul>
      <Link
        href="/campeonatos/novo"
        className="self-start rounded-lg bg-emerald-600 px-5 py-3 font-medium text-white hover:bg-emerald-700"
      >
        Criar meu campeonato
      </Link>
    </main>
  );
}
