package br.com.sumula.aovivo;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
class AoVivoController {

	private final Transmissoes transmissoes;

	AoVivoController(Transmissoes transmissoes) {
		this.transmissoes = transmissoes;
	}

	/** Eventos: {@code conectado} ao abrir e {@code partida} (id da partida) a cada mudança. */
	@GetMapping(path = "/api/v1/campeonatos/{campeonatoId}/ao-vivo", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	SseEmitter acompanhar(@PathVariable UUID campeonatoId) {
		return transmissoes.abrir(campeonatoId);
	}

}
