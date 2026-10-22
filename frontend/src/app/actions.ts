"use server";

import { refresh } from "next/cache";
import { redirect } from "next/navigation";
import { api, ApiError } from "@/lib/api";
import { chaveDoOrganizador, guardarChave } from "@/lib/organizador";

// Toda escrita envia a chave do cookie; quem decide se ela vale é o backend.

export type EstadoForm = { erro?: string };

function mensagem(e: unknown): EstadoForm {
  if (e instanceof ApiError) return { erro: e.message };
  console.error(e);
  return { erro: "Não foi possível falar com o servidor. Tente novamente." };
}

export async function criarCampeonato(_: EstadoForm, form: FormData): Promise<EstadoForm> {
  let id: string;
  try {
    const criado = await api.criarCampeonato(String(form.get("nome") ?? ""), form.get("idaEVolta") === "on");
    id = criado.campeonato.id;
    await guardarChave(id, criado.chaveOrganizador);
  } catch (e) {
    return mensagem(e);
  }
  redirect(`/campeonatos/${id}`); // fora do try: redirect funciona lançando uma exceção
}

export async function inscreverTime(campeonatoId: string, _: EstadoForm, form: FormData): Promise<EstadoForm> {
  try {
    await api.inscreverTime(await chaveDoOrganizador(campeonatoId), campeonatoId, String(form.get("nome") ?? ""));
  } catch (e) {
    return mensagem(e);
  }
  refresh();
  return {};
}

export async function iniciarCampeonato(campeonatoId: string): Promise<EstadoForm> {
  try {
    await api.iniciar(await chaveDoOrganizador(campeonatoId), campeonatoId);
  } catch (e) {
    return mensagem(e);
  }
  refresh();
  return {};
}

export async function registrarResultado(
  campeonatoId: string,
  partidaId: string,
  _: EstadoForm,
  form: FormData,
): Promise<EstadoForm> {
  const gols = (campo: string) => Number.parseInt(String(form.get(campo)), 10);
  try {
    await api.registrarResultado(
      await chaveDoOrganizador(campeonatoId),
      partidaId,
      gols("golsMandante"),
      gols("golsVisitante"),
    );
  } catch (e) {
    return mensagem(e);
  }
  refresh();
  return {};
}
