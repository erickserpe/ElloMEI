package br.com.ellomei.validation.validators;

import br.com.ellomei.validation.anotations.CPF;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CPFValidator implements ConstraintValidator<CPF, String> {

    private final br.com.caelum.stella.validation.CPFValidator stellaValidator = new br.com.caelum.stella.validation.CPFValidator();

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // Consider empty values as valid; use @NotBlank for non-empty checks
        }
        try {
            stellaValidator.assertValid(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
