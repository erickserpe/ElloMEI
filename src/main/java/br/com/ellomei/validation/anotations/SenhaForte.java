package br.com.ellomei.validation.anotations;

import br.com.ellomei.validation.validators.SenhaForteValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Anotação para validar senhas fortes.
 * 
 * Requisitos:
 * - Mínimo de 8 caracteres
 * - Pelo menos 1 letra maiúscula
 * - Pelo menos 1 letra minúscula
 * - Pelo menos 1 número
 * - Pelo menos 1 caractere especial (!@#$%&*)
 * - Não permitir senhas comuns (123456, password, etc)
 * 
 * @author SCF-MEI Team
 * @since 1.0.0
 */
@Documented
@Constraint(validatedBy = SenhaForteValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface SenhaForte {
    String message() default "Senha não atende aos requisitos de segurança";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

