import { ImageResponse } from "next/og";
import { api, type Campeonato, type LinhaClassificacao } from "@/lib/api";

// Imagem do preview quando o link é compartilhado (WhatsApp, Instagram, X...).

export const alt = "Classificação do campeonato no Súmula";
export const size = { width: 1200, height: 630 };
export const contentType = "image/png";

export default async function Image({ params }: { params: Promise<{ slug: string }> }) {
  const { slug } = await params;
  let campeonato: Campeonato | null = null;
  let lideres: LinhaClassificacao[] = [];
  try {
    campeonato = await api.buscarPorSlug(slug);
    if (campeonato.status === "EM_ANDAMENTO") {
      lideres = (await api.classificacao(campeonato.id)).slice(0, 4);
    }
  } catch {
    // sem dados: cai na arte genérica
  }

  return new ImageResponse(
    (
      <div
        style={{
          width: "100%",
          height: "100%",
          display: "flex",
          flexDirection: "column",
          padding: 64,
          background: "linear-gradient(135deg, #064e3b 0%, #047857 100%)",
          color: "white",
          fontFamily: "sans-serif",
        }}
      >
        <div style={{ display: "flex", fontSize: 28, opacity: 0.8 }}>⚽ Súmula</div>
        <div style={{ display: "flex", fontSize: 68, fontWeight: 700, marginTop: 16, lineHeight: 1.1 }}>
          {campeonato?.nome ?? "Campeonato amador"}
        </div>
        {lideres.length > 0 ? (
          <div style={{ display: "flex", flexDirection: "column", marginTop: 40, gap: 12 }}>
            {lideres.map((l) => (
              <div
                key={l.timeId}
                style={{
                  display: "flex",
                  alignItems: "center",
                  fontSize: 34,
                  background: "rgba(255,255,255,0.12)",
                  borderRadius: 16,
                  padding: "12px 24px",
                }}
              >
                <div style={{ display: "flex", width: 56, opacity: 0.7 }}>{l.posicao}º</div>
                <div style={{ display: "flex", flex: 1 }}>{l.time}</div>
                <div style={{ display: "flex", fontWeight: 700 }}>{l.pontos} pts</div>
              </div>
            ))}
          </div>
        ) : (
          <div style={{ display: "flex", fontSize: 36, marginTop: 40, opacity: 0.9 }}>
            {campeonato ? `${campeonato.times.length} times · tabela, placares e classificação` : "Tabela, placares e classificação"}
          </div>
        )}
      </div>
    ),
    size,
  );
}
