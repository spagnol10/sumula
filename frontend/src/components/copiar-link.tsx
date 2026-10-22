"use client";

import { useState } from "react";

/** Copia um caminho do site como URL absoluta. */
export function CopiarLink({ caminho, rotulo }: { caminho: string; rotulo: string }) {
  const [copiado, setCopiado] = useState(false);
  return (
    <button
      type="button"
      onClick={async () => {
        await navigator.clipboard.writeText(new URL(caminho, window.location.origin).toString());
        setCopiado(true);
        setTimeout(() => setCopiado(false), 2000);
      }}
      className="rounded-lg border border-zinc-300 px-3 py-1.5 text-sm font-medium hover:bg-zinc-100 dark:border-zinc-700 dark:hover:bg-zinc-800"
    >
      {copiado ? "Copiado!" : rotulo}
    </button>
  );
}
