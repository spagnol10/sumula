package br.com.sumula.compartilhado;

public class RecursoNaoEncontradoException extends RuntimeException {

	public RecursoNaoEncontradoException(String recurso, Object id) {
		super(recurso + " não encontrado(a): " + id);
	}

}
