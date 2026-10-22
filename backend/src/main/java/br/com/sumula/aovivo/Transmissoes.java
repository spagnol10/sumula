package br.com.sumula.aovivo;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import br.com.sumula.partidas.PartidaAtualizada;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Conexões SSE abertas por campeonato, em memória (ADR 0007).
 *
 * <p>Envia apenas um sinal ("a partida X mudou"); o cliente busca os dados novos pelo caminho
 * normal. Notificações são efêmeras, então não passam pelo outbox de eventos.
 */
@Component
class Transmissoes {

	private static final Logger log = LoggerFactory.getLogger(Transmissoes.class);

	static final Duration TEMPO_MAXIMO_DA_CONEXAO = Duration.ofMinutes(30);

	/** Protege a VM gratuita de esgotar memória ou threads. */
	static final int LIMITE_DE_CONEXOES = 2_000;

	private final Map<UUID, Set<SseEmitter>> porCampeonato = new ConcurrentHashMap<>();

	private final AtomicInteger abertas = new AtomicInteger();

	SseEmitter abrir(UUID campeonatoId) {
		if (abertas.get() >= LIMITE_DE_CONEXOES) {
			throw new ServidorLotadoException();
		}
		SseEmitter emitter = new SseEmitter(TEMPO_MAXIMO_DA_CONEXAO.toMillis());
		Set<SseEmitter> conexoes = porCampeonato.computeIfAbsent(campeonatoId, id -> ConcurrentHashMap.newKeySet());
		conexoes.add(emitter);
		abertas.incrementAndGet();

		Runnable remover = () -> {
			if (conexoes.remove(emitter)) {
				abertas.decrementAndGet();
			}
		};
		emitter.onCompletion(remover);
		emitter.onTimeout(remover);
		emitter.onError(e -> remover.run());

		enviar(emitter, SseEmitter.event().name("conectado").data(campeonatoId.toString()));
		return emitter;
	}

	/** Dispara após o commit, para o cliente nunca ler um estado que ainda pode sofrer rollback. */
	@EventListener
	void quandoPartidaMudar(PartidaAtualizada evento) {
		Runnable avisar = () -> avisar(evento);
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					avisar.run();
				}
			});
		}
		else {
			avisar.run();
		}
	}

	private void avisar(PartidaAtualizada evento) {
		Set<SseEmitter> conexoes = porCampeonato.getOrDefault(evento.campeonatoId(), Set.of());
		conexoes.forEach(e -> enviar(e, SseEmitter.event().name("partida").data(evento.partidaId().toString())));
		log.debug("Partida {} atualizada; {} conexões avisadas", evento.partidaId(), conexoes.size());
	}

	/** Comentário SSE periódico para proxies não derrubarem conexões ociosas. */
	@Scheduled(fixedRate = 20_000)
	void manterVivas() {
		porCampeonato.values().forEach(conexoes -> conexoes.forEach(e -> enviar(e, SseEmitter.event().comment("ping"))));
	}

	int abertas() {
		return abertas.get();
	}

	private static void enviar(SseEmitter emitter, SseEmitter.SseEventBuilder evento) {
		try {
			emitter.send(evento);
		}
		catch (IOException | IllegalStateException e) {
			// cliente desconectou; o callback de erro/conclusão remove a conexão
			emitter.completeWithError(e);
		}
	}

}
