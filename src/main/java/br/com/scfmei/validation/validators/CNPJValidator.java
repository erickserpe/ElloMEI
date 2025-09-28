package br.com.scfmei.validation.validators;

import br.com.scfmei.validation.anotations.CNPJ;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CNPJValidator implements ConstraintValidator<CNPJ, String> {

    private final br.com.caelum.stella.validation.CNPJValidator stellaValidator = new br.com.caelum.stella.validation.CNPJValidator();

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
