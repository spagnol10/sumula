import type { Metadata } from "next";
import { NovoCampeonatoForm } from "@/components/forms";

export const metadata: Metadata = { title: "Novo campeonato · Súmula" };

export default function NovoCampeonato() {
  return (
    <main className="mx-auto flex w-full max-w-md flex-1 flex-col gap-6 px-4 py-12">
      <h1 className="text-2xl font-semibold">Novo campeonato</h1>
      <NovoCampeonatoForm />
    </main>
  );
}
