"use client";

import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

/**
 * Escuta o SSE do campeonato e recarrega os dados da página quando uma partida muda (ADR 0007).
 * O EventSource reconecta sozinho se a conexão cair.
 */
export function AoVivo({ campeonatoId }: { campeonatoId: string }) {
  const router = useRouter();
  const [conectado, setConectado] = useState(false);

  useEffect(() => {
    const fonte = new EventSource(`${API_URL}/api/v1/campeonatos/${campeonatoId}/ao-vivo`);
    let pendente: ReturnType<typeof setTimeout> | undefined;

    fonte.addEventListener("conectado", () => setConectado(true));
    fonte.addEventListener("partida", () => {
      // agrupa rajadas de eventos (ex.: gol + cartão) num único refresh
      clearTimeout(pendente);
      pendente = setTimeout(() => router.refresh(), 300);
    });
    fonte.onerror = () => setConectado(false);

    return () => {
      clearTimeout(pendente);
      fonte.close();
    };
  }, [campeonatoId, router]);

  return (
    <span
      className={`inline-flex items-center gap-1.5 text-xs font-medium ${conectado ? "text-red-600 dark:text-red-400" : "text-zinc-400"}`}
      title={conectado ? "Atualizando em tempo real" : "Reconectando…"}
    >
      <span className={`size-2 rounded-full ${conectado ? "animate-pulse bg-red-600" : "bg-zinc-400"}`} />
      {conectado ? "Ao vivo" : "Offline"}
    </span>
  );
}
