package br.com.sumula;

import java.nio.file.Path;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.boot.SpringApplication;

/**
 * Sobe a API com um Postgres 17 embutido, para rodar localmente sem Docker.
 * Os dados ficam em {@code target/dev-postgres} e sobrevivem entre execuções (somem no {@code mvn clean}).
 *
 * <pre>./mvnw spring-boot:test-run -Dspring-boot.run.main-class=br.com.sumula.DevLocal</pre>
 */
public class DevLocal {

	public static void main(String[] args) throws Exception {
		EmbeddedPostgres postgres = EmbeddedPostgres.builder()
			.setPort(5433)
			.setDataDirectory(Path.of("target", "dev-postgres"))
			.setCleanDataDirectory(false)
			.start();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				postgres.close();
			}
			catch (Exception e) {
				// encerrando de qualquer forma
			}
		}));

		System.setProperty("spring.datasource.url", postgres.getJdbcUrl("postgres", "postgres"));
		System.setProperty("spring.datasource.username", "postgres");
		System.setProperty("spring.datasource.password", "");
		SpringApplication.run(SumulaApiApplication.class, args);
	}

}
