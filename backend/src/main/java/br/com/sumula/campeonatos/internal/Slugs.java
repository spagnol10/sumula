package br.com.sumula.campeonatos.internal;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.Locale;

/** Gera identificadores legíveis para URLs públicas, ex.: "copa-da-vila-2026-k3x9q2". */
final class Slugs {

	private static final String ALFABETO = "abcdefghjkmnpqrstuvwxyz23456789";

	private static final SecureRandom RANDOM = new SecureRandom();

	private Slugs() {
	}

	static String gerar(String texto) {
		String base = Normalizer.normalize(texto, Normalizer.Form.NFD)
			.replaceAll("\\p{M}", "")
			.toLowerCase(Locale.ROOT)
			.replaceAll("[^a-z0-9]+", "-")
			.replaceAll("(^-+|-+$)", "");
		if (base.length() > 60) {
			base = base.substring(0, 60).replaceAll("-+$", "");
		}
		return (base.isEmpty() ? "campeonato" : base) + "-" + sufixo();
	}

	private static String sufixo() {
		StringBuilder sb = new StringBuilder(6);
		for (int i = 0; i < 6; i++) {
			sb.append(ALFABETO.charAt(RANDOM.nextInt(ALFABETO.length())));
		}
		return sb.toString();
	}

}
