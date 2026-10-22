import Link from "next/link";

export default function NaoEncontrado() {
  return (
    <main className="mx-auto flex w-full max-w-md flex-1 flex-col items-start gap-4 px-4 py-16">
      <h1 className="text-2xl font-semibold">Campeonato não encontrado</h1>
      <Link href="/" className="text-emerald-700 underline dark:text-emerald-400">
        Voltar para o início
      </Link>
    </main>
  );
}
