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

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${API_URL}/api/v1${path}`, {
    ...init,
    headers: { "Content-Type": "application/json", ...init?.headers },
  });
  if (!res.ok) {
    const problem = await res.json().catch(() => null);
    throw new ApiError(res.status, problem?.detail ?? "Não foi possível completar a operação.");
  }
  return res.json() as Promise<T>;
}

export const api = {
  buscarCampeonato: (id: string) => request<Campeonato>(`/campeonatos/${id}`),
  listarPartidas: (id: string) => request<Partida[]>(`/campeonatos/${id}/partidas`),
  classificacao: (id: string) => request<LinhaClassificacao[]>(`/campeonatos/${id}/classificacao`),

  criarCampeonato: (nome: string, idaEVolta: boolean) =>
    request<Campeonato>("/campeonatos", { method: "POST", body: JSON.stringify({ nome, idaEVolta }) }),
  inscreverTime: (campeonatoId: string, nome: string) =>
    request<Time>(`/campeonatos/${campeonatoId}/times`, { method: "POST", body: JSON.stringify({ nome }) }),
  iniciar: (campeonatoId: string) =>
    request<Campeonato>(`/campeonatos/${campeonatoId}/iniciar`, { method: "POST" }),
  registrarResultado: (partidaId: string, golsMandante: number, golsVisitante: number) =>
    request<Partida>(`/partidas/${partidaId}/resultado`, {
      method: "PUT",
      body: JSON.stringify({ golsMandante, golsVisitante }),
    }),
};
