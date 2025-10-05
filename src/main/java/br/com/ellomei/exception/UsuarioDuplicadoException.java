package br.com.ellomei.exception;

/**
 * Exception lançada quando há tentativa de cadastrar um usuário
 * com username ou email já existente no sistema.
 * 
 * @author SCF-MEI Team
 * @since 1.0.0
 */
public class UsuarioDuplicadoException extends RuntimeException {
    
    public UsuarioDuplicadoException(String mensagem) {
        super(mensagem);
    }
    
    public UsuarioDuplicadoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}

