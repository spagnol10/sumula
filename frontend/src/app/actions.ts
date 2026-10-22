"use server";

import { refresh } from "next/cache";
import { redirect } from "next/navigation";
import { api, ApiError, type TipoDeEvento } from "@/lib/api";
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

const textoOuNulo = (form: FormData, campo: string) => {
  const valor = String(form.get(campo) ?? "").trim();
  return valor === "" ? null : valor;
};

const numeroOuNulo = (form: FormData, campo: string) => {
  const valor = textoOuNulo(form, campo);
  return valor === null ? null : Number.parseInt(valor, 10);
};

/** Executa uma escrita com a chave do campeonato e atualiza a tela. */
async function comChave(
  campeonatoId: string,
  escrita: (chave: string | undefined) => Promise<unknown>,
): Promise<EstadoForm> {
  try {
    await escrita(await chaveDoOrganizador(campeonatoId));
  } catch (e) {
    return mensagem(e);
  }
  refresh();
  return {};
}

export async function inscreverJogador(
  campeonatoId: string,
  timeId: string,
  _: EstadoForm,
  form: FormData,
): Promise<EstadoForm> {
  return comChave(campeonatoId, (chave) =>
    api.inscreverJogador(chave, campeonatoId, timeId, String(form.get("nome") ?? ""), numeroOuNulo(form, "numero")),
  );
}

export async function iniciarPartida(campeonatoId: string, partidaId: string): Promise<EstadoForm> {
  return comChave(campeonatoId, (chave) => api.iniciarPartida(chave, partidaId));
}

export async function encerrarPartida(campeonatoId: string, partidaId: string): Promise<EstadoForm> {
  return comChave(campeonatoId, (chave) => api.encerrarPartida(chave, partidaId));
}

/** O botão clicado define o tipo (name="tipo"); o formulário de cada time define o time. */
export async function registrarEvento(
  campeonatoId: string,
  partidaId: string,
  timeId: string,
  _: EstadoForm,
  form: FormData,
): Promise<EstadoForm> {
  return comChave(campeonatoId, (chave) =>
    api.registrarEvento(chave, partidaId, {
      tipo: String(form.get("tipo")) as TipoDeEvento,
      timeId,
      jogadorId: textoOuNulo(form, "jogadorId"),
      minuto: numeroOuNulo(form, "minuto"),
    }),
  );
}

export async function removerEvento(campeonatoId: string, partidaId: string, eventoId: string): Promise<EstadoForm> {
  return comChave(campeonatoId, (chave) => api.removerEvento(chave, partidaId, eventoId));
}
