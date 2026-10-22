package br.com.sumula.campeonatos.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SlugsTest {

	@Test
	void removeAcentosESimbolos() {
		assertThat(Slugs.gerar("Copa São João — Várzea 2026!")).matches("copa-sao-joao-varzea-2026-[a-z2-9]{6}");
	}

	@Test
	void usaPadraoQuandoNaoSobraNada() {
		assertThat(Slugs.gerar("⚽⚽")).matches("campeonato-[a-z2-9]{6}");
	}

	@Test
	void limitaTamanho() {
		assertThat(Slugs.gerar("a".repeat(200))).hasSize(60 + 1 + 6);
	}

	@Test
	void geraSufixosDiferentes() {
		assertThat(Slugs.gerar("Copa")).isNotEqualTo(Slugs.gerar("Copa"));
	}

}
