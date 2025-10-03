package br.com.scfmei.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando um usuário tenta exceder os limites do seu plano de assinatura.
 * 
 * Esta exceção é usada principalmente para o plano FREE, que tem limite de 20 lançamentos mensais.
 * Quando lançada, retorna HTTP 403 (Forbidden) para o cliente.
 * 
 * @author SCF-MEI Team
 * @since 1.0.0
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class PlanLimitExceededException extends RuntimeException {
    
    /**
     * Cria uma nova exceção com a mensagem especificada.
     * 
     * @param message Mensagem descritiva do limite excedido
     */
    public PlanLimitExceededException(String message) {
        super(message);
    }
}

