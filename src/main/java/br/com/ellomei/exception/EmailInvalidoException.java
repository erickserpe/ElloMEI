package br.com.ellomei.exception;

/**
 * Exception lançada quando um email não passa nas validações
 * de formato, domínio ou registros MX.
 * 
 * @author ElloMEI Team
 * @since 1.0.0
 */
public class EmailInvalidoException extends RuntimeException {
    
    public EmailInvalidoException(String mensagem) {
        super(mensagem);
    }
    
    public EmailInvalidoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}

