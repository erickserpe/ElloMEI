package br.com.scfmei.validation.anotations;

import br.com.scfmei.validation.validators.EmailValidoValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Anotação para validar emails reais.
 * 
 * Não apenas valida o formato, mas também:
 * - Verifica se o domínio existe (DNS)
 * - Verifica se o domínio tem registros MX (pode receber emails)
 * 
 * @author SCF-MEI Team
 * @since 1.0.0
 */
@Documented
@Constraint(validatedBy = EmailValidoValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface EmailValido {
    String message() default "Email inválido ou inexistente";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

