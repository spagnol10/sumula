import { type NextRequest, NextResponse } from "next/server";
import { api } from "@/lib/api";
import { guardarChave } from "@/lib/organizador";

/**
 * Link de acesso do organizador: /campeonatos/{id}/acesso?chave=...
 * Valida a chave na API, guarda no cookie e redireciona, tirando a chave da barra de endereços.
 */
export async function GET(request: NextRequest, ctx: RouteContext<"/campeonatos/[id]/acesso">) {
  const { id } = await ctx.params;
  const chave = request.nextUrl.searchParams.get("chave");
  if (chave && (await api.chaveValida(id, chave))) {
    await guardarChave(id, chave);
  }
  return NextResponse.redirect(new URL(`/campeonatos/${id}`, request.url));
}
