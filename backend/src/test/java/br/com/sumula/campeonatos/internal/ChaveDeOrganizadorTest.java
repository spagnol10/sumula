package br.com.sumula.campeonatos.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ChaveDeOrganizadorTest {

	@Test
	void geraChaveForteEGuardaApenasOHash() {
		ChaveDeOrganizador.Gerada chave = ChaveDeOrganizador.gerar();

		assertThat(chave.valor()).hasSize(43).matches("[A-Za-z0-9_-]+"); // 256 bits em base64url
		assertThat(chave.hash()).hasSize(64).doesNotContain(chave.valor());
		assertThat(ChaveDeOrganizador.confere(chave.valor(), chave.hash())).isTrue();
	}

	@Test
	void recusaChaveErradaOuVazia() {
		ChaveDeOrganizador.Gerada chave = ChaveDeOrganizador.gerar();

		assertThat(ChaveDeOrganizador.confere(ChaveDeOrganizador.gerar().valor(), chave.hash())).isFalse();
		assertThat(ChaveDeOrganizador.confere(null, chave.hash())).isFalse();
		assertThat(ChaveDeOrganizador.confere(" ", chave.hash())).isFalse();
	}

}
