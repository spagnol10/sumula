import { cookies } from "next/headers";

// Chave de organizador guardada num cookie httpOnly por campeonato (ADR 0006).
// O JavaScript do navegador nunca enxerga a chave; só o servidor do Next a envia à API.

const nomeDoCookie = (campeonatoId: string) => `org_${campeonatoId}`;

export async function chaveDoOrganizador(campeonatoId: string) {
  return (await cookies()).get(nomeDoCookie(campeonatoId))?.value;
}

/** Só pode ser chamada em Server Actions ou Route Handlers. */
export async function guardarChave(campeonatoId: string, chave: string) {
  (await cookies()).set(nomeDoCookie(campeonatoId), chave, {
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    sameSite: "lax",
    path: "/",
    maxAge: 60 * 60 * 24 * 400, // limite máximo aceito pelos navegadores
  });
}
