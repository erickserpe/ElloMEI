package br.com.scfmei.validation.validators;

import br.com.scfmei.validation.anotations.SenhaForte;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validador de senhas fortes.
 * 
 * Implementa validações de segurança para senhas:
 * - Comprimento mínimo
 * - Complexidade (maiúsculas, minúsculas, números, caracteres especiais)
 * - Lista negra de senhas comuns
 * 
 * @author SCF-MEI Team
 * @since 1.0.0
 */
public class SenhaForteValidator implements ConstraintValidator<SenhaForte, String> {

    // Padrões de validação
    private static final int TAMANHO_MINIMO = 8;
    private static final Pattern LETRA_MAIUSCULA = Pattern.compile("[A-Z]");
    private static final Pattern LETRA_MINUSCULA = Pattern.compile("[a-z]");
    private static final Pattern NUMERO = Pattern.compile("[0-9]");
    private static final Pattern CARACTERE_ESPECIAL = Pattern.compile("[!@#$%&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");

    // Lista de senhas comuns que devem ser bloqueadas
    private static final List<String> SENHAS_COMUNS = Arrays.asList(
        "123456", "123456789", "12345678", "1234567890",
        "password", "senha", "senha123", "password123",
        "qwerty", "abc123", "111111", "123123",
        "admin", "admin123", "root", "root123",
        "12345", "1234", "654321", "000000",
        "abcdef", "abcd1234", "qwerty123", "letmein",
        "welcome", "monkey", "dragon", "master",
        "sunshine", "princess", "football", "iloveyou"
    );

    @Override
    public boolean isValid(String senha, ConstraintValidatorContext context) {
        // Permite valores nulos (use @NotNull/@NotBlank para obrigatoriedade)
        if (senha == null || senha.isBlank()) {
            return true;
        }

        // Desabilita a mensagem padrão
        context.disableDefaultConstraintViolation();

        // Validação 1: Tamanho mínimo
        if (senha.length() < TAMANHO_MINIMO) {
            context.buildConstraintViolationWithTemplate(
                "A senha deve ter no mínimo " + TAMANHO_MINIMO + " caracteres"
            ).addConstraintViolation();
            return false;
        }

        // Validação 2: Letra maiúscula
        if (!LETRA_MAIUSCULA.matcher(senha).find()) {
            context.buildConstraintViolationWithTemplate(
                "A senha deve conter pelo menos uma letra maiúscula"
            ).addConstraintViolation();
            return false;
        }

        // Validação 3: Letra minúscula
        if (!LETRA_MINUSCULA.matcher(senha).find()) {
            context.buildConstraintViolationWithTemplate(
                "A senha deve conter pelo menos uma letra minúscula"
            ).addConstraintViolation();
            return false;
        }

        // Validação 4: Número
        if (!NUMERO.matcher(senha).find()) {
            context.buildConstraintViolationWithTemplate(
                "A senha deve conter pelo menos um número"
            ).addConstraintViolation();
            return false;
        }

        // Validação 5: Caractere especial
        if (!CARACTERE_ESPECIAL.matcher(senha).find()) {
            context.buildConstraintViolationWithTemplate(
                "A senha deve conter pelo menos um caractere especial (!@#$%&*)"
            ).addConstraintViolation();
            return false;
        }

        // Validação 6: Senhas comuns
        String senhaLower = senha.toLowerCase();
        for (String senhaComum : SENHAS_COMUNS) {
            if (senhaLower.contains(senhaComum.toLowerCase())) {
                context.buildConstraintViolationWithTemplate(
                    "Esta senha é muito comum e não pode ser usada. Escolha uma senha mais segura"
                ).addConstraintViolation();
                return false;
            }
        }

        // Validação 7: Sequências óbvias
        if (contemSequenciaObvia(senha)) {
            context.buildConstraintViolationWithTemplate(
                "A senha não pode conter sequências óbvias (ex: 123, abc, aaa)"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }

    /**
     * Verifica se a senha contém sequências óbvias.
     */
    private boolean contemSequenciaObvia(String senha) {
        String senhaLower = senha.toLowerCase();
        
        // Verifica sequências numéricas (123, 234, etc)
        for (int i = 0; i < senhaLower.length() - 2; i++) {
            char c1 = senhaLower.charAt(i);
            char c2 = senhaLower.charAt(i + 1);
            char c3 = senhaLower.charAt(i + 2);
            
            if (Character.isDigit(c1) && Character.isDigit(c2) && Character.isDigit(c3)) {
                if (c2 == c1 + 1 && c3 == c2 + 1) {
                    return true; // Sequência crescente: 123, 234, etc
                }
                if (c2 == c1 - 1 && c3 == c2 - 1) {
                    return true; // Sequência decrescente: 321, 432, etc
                }
            }
            
            // Verifica sequências alfabéticas (abc, bcd, etc)
            if (Character.isLetter(c1) && Character.isLetter(c2) && Character.isLetter(c3)) {
                if (c2 == c1 + 1 && c3 == c2 + 1) {
                    return true; // Sequência crescente: abc, bcd, etc
                }
                if (c2 == c1 - 1 && c3 == c2 - 1) {
                    return true; // Sequência decrescente: cba, dcb, etc
                }
            }
            
            // Verifica repetições (aaa, 111, etc)
            if (c1 == c2 && c2 == c3) {
                return true;
            }
        }
        
        return false;
    }
}

