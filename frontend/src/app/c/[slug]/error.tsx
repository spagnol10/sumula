"use client";

export default function Erro({ reset }: { error: Error; reset: () => void }) {
  return (
    <main className="mx-auto flex w-full max-w-md flex-1 flex-col items-start gap-4 px-4 py-16">
      <h1 className="text-2xl font-semibold">Não foi possível carregar o campeonato</h1>
      <p className="text-zinc-600 dark:text-zinc-400">O servidor pode estar iniciando. Tente de novo em instantes.</p>
      <button onClick={reset} className="rounded-lg bg-emerald-600 px-4 py-2 font-medium text-white hover:bg-emerald-700">
        Tentar de novo
      </button>
    </main>
  );
}
