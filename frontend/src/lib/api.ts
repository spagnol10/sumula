// Cliente da API do backend. Usado apenas no servidor (Server Components e Server Actions):
// o navegador nunca fala direto com o Spring.

export type StatusCampeonato = "INSCRICOES_ABERTAS" | "EM_ANDAMENTO";

export type Jogador = { id: string; nome: string; numero: number | null };

export type Time = { id: string; nome: string };

export type TimeInscrito = Time & { jogadores: Jogador[] };

export type Campeonato = {
  id: string;
  nome: string;
  slug: string;
  idaEVolta: boolean;
  status: StatusCampeonato;
  criadoEm: string;
  times: TimeInscrito[];
};

export type CampeonatoCriado = { campeonato: Campeonato; chaveOrganizador: string };

export type TipoDeEvento = "GOL" | "CARTAO_AMARELO" | "CARTAO_VERMELHO";

export type EventoDaPartida = {
  id: string;
  tipo: TipoDeEvento;
  timeId: string;
  jogadorId: string | null;
  jogador: string | null;
  minuto: number | null;
};

export type Partida = {
  id: string;
  campeonatoId: string;
  rodada: number;
  mandante: Time;
  visitante: Time;
  golsMandante: number | null;
  golsVisitante: number | null;
  status: "AGENDADA" | "EM_ANDAMENTO" | "ENCERRADA";
  placarPelaSumula: boolean;
  eventos: EventoDaPartida[];
};

export type LinhaArtilharia = {
  posicao: number;
  jogadorId: string;
  jogador: string;
  timeId: string;
  time: string;
  gols: number;
};

export type Suspensao = {
  jogadorId: string;
  jogador: string;
  timeId: string;
  time: string;
  motivo: "CARTAO_VERMELHO" | "ACUMULO_DE_AMARELOS";
  rodadaDoCartao: number;
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
  artilharia: (id: string) => request<LinhaArtilharia[]>(`/campeonatos/${id}/artilharia`),
  suspensoes: (id: string) => request<Suspensao[]>(`/campeonatos/${id}/suspensoes`),
  buscarPartida: (id: string) => request<Partida>(`/partidas/${id}`),

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
  inscreverJogador: (chave: string | undefined, campeonatoId: string, timeId: string, nome: string, numero: number | null) =>
    request<Jogador>(`/campeonatos/${campeonatoId}/times/${timeId}/jogadores`, {
      method: "POST",
      chave,
      body: JSON.stringify({ nome, numero }),
    }),
  iniciar: (chave: string | undefined, campeonatoId: string) =>
    request<Campeonato>(`/campeonatos/${campeonatoId}/iniciar`, { method: "POST", chave }),
  registrarResultado: (chave: string | undefined, partidaId: string, golsMandante: number, golsVisitante: number) =>
    request<Partida>(`/partidas/${partidaId}/resultado`, {
      method: "PUT",
      chave,
      body: JSON.stringify({ golsMandante, golsVisitante }),
    }),
  iniciarPartida: (chave: string | undefined, partidaId: string) =>
    request<Partida>(`/partidas/${partidaId}/inicio`, { method: "POST", chave }),
  encerrarPartida: (chave: string | undefined, partidaId: string) =>
    request<Partida>(`/partidas/${partidaId}/encerramento`, { method: "POST", chave }),
  registrarEvento: (
    chave: string | undefined,
    partidaId: string,
    evento: { tipo: TipoDeEvento; timeId: string; jogadorId: string | null; minuto: number | null },
  ) => request<Partida>(`/partidas/${partidaId}/eventos`, { method: "POST", chave, body: JSON.stringify(evento) }),
  removerEvento: (chave: string | undefined, partidaId: string, eventoId: string) =>
    request<Partida>(`/partidas/${partidaId}/eventos/${eventoId}`, { method: "DELETE", chave }),
};
