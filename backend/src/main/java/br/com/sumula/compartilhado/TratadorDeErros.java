package br.com.sumula.compartilhado;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Traduz exceções de domínio para respostas RFC 9457 (Problem Details). */
@RestControllerAdvice
class TratadorDeErros extends ResponseEntityExceptionHandler {

	@ExceptionHandler
	ProblemDetail naoEncontrado(RecursoNaoEncontradoException e) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
	}

	@ExceptionHandler
	ProblemDetail regraDeNegocio(RegraDeNegocioException e) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
	}

	@ExceptionHandler
	ProblemDetail conflito(ObjectOptimisticLockingFailureException e) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
				"O recurso foi alterado por outra requisição. Recarregue e tente novamente.");
	}

}
