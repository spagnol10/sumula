// Cliente da API do backend. Usado apenas no servidor (Server Components e Server Actions):
// o navegador nunca fala direto com o Spring.

export type StatusCampeonato = "INSCRICOES_ABERTAS" | "EM_ANDAMENTO";

export type Time = { id: string; nome: string };

export type Campeonato = {
  id: string;
  nome: string;
  slug: string;
  idaEVolta: boolean;
  status: StatusCampeonato;
  criadoEm: string;
  times: Time[];
};

export type CampeonatoCriado = { campeonato: Campeonato; chaveOrganizador: string };

export type Partida = {
  id: string;
  rodada: number;
  mandante: Time;
  visitante: Time;
  golsMandante: number | null;
  golsVisitante: number | null;
  status: "AGENDADA" | "ENCERRADA";
};

export type LinhaClassificacao = {
  posicao: number;
  timeId: string;
  time: string;
  pontos: number;
  jogos: number;
  vitorias: number;
  empates: number;
  derrotas: number;
  golsPro: number;
  golsContra: number;
  saldo: number;
  aproveitamento: number;
};

/** Erro no formato Problem Details (RFC 9457) devolvido pelo backend. */
export class ApiError extends Error {
  constructor(
    readonly status: number,
    message: string,
  ) {
    super(message);
  }
}

const API_URL = process.env.API_URL ?? "http://localhost:8080";

async function request<T>(path: string, init?: RequestInit & { chave?: string }): Promise<T> {
  const { chave, ...resto } = init ?? {};
  const res = await fetch(`${API_URL}/api/v1${path}`, {
    ...resto,
    headers: {
      "Content-Type": "application/json",
      ...(chave ? { "X-Chave-Organizador": chave } : {}),
      ...resto.headers,
    },
  });
  if (!res.ok) {
    const problem = await res.json().catch(() => null);
    throw new ApiError(res.status, problem?.detail ?? "Não foi possível completar a operação.");
  }
  return (res.status === 204 ? undefined : res.json()) as Promise<T>;
}

export const api = {
  buscarCampeonato: (id: string) => request<Campeonato>(`/campeonatos/${id}`),
  buscarPorSlug: (slug: string) => request<Campeonato>(`/campeonatos/slug/${encodeURIComponent(slug)}`),
  listarPartidas: (id: string) => request<Partida[]>(`/campeonatos/${id}/partidas`),
  classificacao: (id: string) => request<LinhaClassificacao[]>(`/campeonatos/${id}/classificacao`),

  criarCampeonato: (nome: string, idaEVolta: boolean) =>
    request<CampeonatoCriado>("/campeonatos", { method: "POST", body: JSON.stringify({ nome, idaEVolta }) }),
  async chaveValida(campeonatoId: string, chave: string) {
    try {
      await request<void>(`/campeonatos/${campeonatoId}/chave/verificacao`, { method: "POST", chave });
      return true;
    } catch (e) {
      if (e instanceof ApiError && (e.status === 403 || e.status === 404)) return false;
      throw e;
    }
  },

  // Escritas: exigem a chave do organizador (ADR 0006)
  inscreverTime: (chave: string | undefined, campeonatoId: string, nome: string) =>
    request<Time>(`/campeonatos/${campeonatoId}/times`, { method: "POST", chave, body: JSON.stringify({ nome }) }),
  iniciar: (chave: string | undefined, campeonatoId: string) =>
    request<Campeonato>(`/campeonatos/${campeonatoId}/iniciar`, { method: "POST", chave }),
  registrarResultado: (chave: string | undefined, partidaId: string, golsMandante: number, golsVisitante: number) =>
    request<Partida>(`/partidas/${partidaId}/resultado`, {
      method: "PUT",
      chave,
      body: JSON.stringify({ golsMandante, golsVisitante }),
    }),
};
