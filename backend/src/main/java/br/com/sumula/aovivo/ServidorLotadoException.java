package br.com.sumula.aovivo;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
class ServidorLotadoException extends RuntimeException {

	ServidorLotadoException() {
		super("Muitas conexões ao vivo abertas. Tente novamente em instantes.");
	}

}
