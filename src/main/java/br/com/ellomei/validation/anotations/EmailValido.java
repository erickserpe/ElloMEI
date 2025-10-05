package br.com.ellomei.validation.anotations;

import br.com.ellomei.validation.validators.EmailValidoValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação para validar emails reais.
 * 
 * Não apenas valida o formato, mas também:
 * - Verifica se o domínio existe (DNS)
 * - Verifica se o domínio tem registros MX (pode receber emails)
 * 
 * @author ElloMEI Team
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

