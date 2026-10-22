package br.com.sumula.campeonatos.internal;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Segredo que autoriza alterações num campeonato (ADR 0006). Só o hash SHA-256 é persistido;
 * o valor em claro é devolvido uma única vez, na criação.
 */
final class ChaveDeOrganizador {

	private static final SecureRandom RANDOM = new SecureRandom();

	record Gerada(String valor, String hash) {
	}

	private ChaveDeOrganizador() {
	}

	static Gerada gerar() {
		byte[] bytes = new byte[32];
		RANDOM.nextBytes(bytes);
		String valor = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		return new Gerada(valor, hash(valor));
	}

	static String hash(String valor) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		}
		catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	/** Comparação em tempo constante para não vazar o hash por timing. */
	static boolean confere(String valor, String hashEsperado) {
		if (valor == null || valor.isBlank()) {
			return false;
		}
		return MessageDigest.isEqual(hash(valor).getBytes(StandardCharsets.US_ASCII),
				hashEsperado.getBytes(StandardCharsets.US_ASCII));
	}

}
