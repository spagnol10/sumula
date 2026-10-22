"use client";

import { useRouter } from "next/navigation";
import { useEffect } from "react";

/** Recarrega os dados da página até a tabela (gerada de forma assíncrona) aparecer. */
export function AtualizacaoAutomatica({ intervaloMs = 1500 }: { intervaloMs?: number }) {
  const router = useRouter();
  useEffect(() => {
    const id = setInterval(() => router.refresh(), intervaloMs);
    return () => clearInterval(id);
  }, [router, intervaloMs]);
  return null;
}
