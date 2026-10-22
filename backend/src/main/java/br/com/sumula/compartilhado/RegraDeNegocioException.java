package br.com.sumula.compartilhado;

/** Operação válida sintaticamente, mas proibida pelo estado atual do domínio. */
public class RegraDeNegocioException extends RuntimeException {

	public RegraDeNegocioException(String mensagem) {
		super(mensagem);
	}

}
