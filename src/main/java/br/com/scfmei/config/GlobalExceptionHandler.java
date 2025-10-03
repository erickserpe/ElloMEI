package br.com.scfmei.config;

import br.com.scfmei.exception.PlanLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

/**
 * Handler global de exceções para a aplicação.
 * 
 * Esta classe centraliza o tratamento de exceções, garantindo que
 * respostas HTTP adequadas sejam retornadas para o frontend.
 * 
 * Benefícios:
 * - Respostas consistentes para todas as exceções
 * - Separação de concerns (lógica de negócio vs tratamento de erros)
 * - Facilita manutenção e adição de novos handlers
 * 
 * @author SCF-MEI Team
 * @since 1.0.0
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Trata exceções de limite de plano excedido.
     * 
     * Quando um usuário do plano FREE tenta exceder o limite de 20 lançamentos mensais,
     * esta exceção é lançada e capturada aqui, retornando uma resposta HTTP 403 Forbidden
     * com uma mensagem clara para o frontend.
     * 
     * @param ex A exceção de limite de plano excedido
     * @return ResponseEntity com status 403 e corpo JSON contendo erro e mensagem
     */
    @ExceptionHandler(PlanLimitExceededException.class)
    public ResponseEntity<Object> handlePlanLimitExceeded(PlanLimitExceededException ex) {
        Map<String, String> body = Map.of(
            "error", "Limite do Plano Excedido",
            "message", ex.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }
}

