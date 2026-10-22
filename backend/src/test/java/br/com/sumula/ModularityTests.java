package br.com.sumula;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Garante que os módulos (pacotes diretos de br.com.sumula) só se comunicam
 * pelas APIs públicas uns dos outros. Ver docs/adr/0002-monolito-modular.md.
 */
class ModularityTests {

	private final ApplicationModules modules = ApplicationModules.of(SumulaApiApplication.class);

	@Test
	void verificaFronteirasDosModulos() {
		modules.verify();
	}

	@Test
	void geraDocumentacaoDosModulos() {
		new Documenter(modules).writeDocumentation();
	}

}
