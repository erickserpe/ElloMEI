package br.com.ellomei.validation.validators;

import br.com.ellomei.validation.anotations.EmailValido;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;
import java.util.regex.Pattern;

/**
 * Validador de emails reais.
 * 
 * Implementa validações avançadas:
 * 1. Validação de formato (regex)
 * 2. Verificação de existência do domínio (DNS)
 * 3. Verificação de registros MX (capacidade de receber emails)
 * 
 * @author SCF-MEI Team
 * @since 1.0.0
 */
public class EmailValidoValidator implements ConstraintValidator<EmailValido, String> {

    private static final Logger logger = LoggerFactory.getLogger(EmailValidoValidator.class);

    // Padrão RFC 5322 simplificado para validação de email
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        // Permite valores nulos (use @NotNull/@NotBlank para obrigatoriedade)
        if (email == null || email.isBlank()) {
            return true;
        }

        // Desabilita a mensagem padrão
        context.disableDefaultConstraintViolation();

        // Validação 1: Formato do email
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            context.buildConstraintViolationWithTemplate(
                "Formato de email inválido"
            ).addConstraintViolation();
            return false;
        }

        // Extrai o domínio do email
        String dominio = extrairDominio(email);
        if (dominio == null) {
            context.buildConstraintViolationWithTemplate(
                "Email inválido: não foi possível extrair o domínio"
            ).addConstraintViolation();
            return false;
        }

        // Validação 2: Verifica se o domínio existe e tem registros MX
        if (!verificarDominioMX(dominio)) {
            context.buildConstraintViolationWithTemplate(
                "Email inválido: o domínio '" + dominio + "' não existe ou não pode receber emails"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }

    /**
     * Extrai o domínio de um endereço de email.
     * 
     * @param email Email completo
     * @return Domínio ou null se inválido
     */
    private String extrairDominio(String email) {
        int atIndex = email.lastIndexOf('@');
        if (atIndex == -1 || atIndex == email.length() - 1) {
            return null;
        }
        return email.substring(atIndex + 1);
    }

    /**
     * Verifica se o domínio existe e possui registros MX (Mail Exchange).
     * 
     * Registros MX indicam que o domínio está configurado para receber emails.
     * 
     * @param dominio Domínio a ser verificado
     * @return true se o domínio tem registros MX válidos
     */
    private boolean verificarDominioMX(String dominio) {
        try {
            // Configuração do contexto DNS
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            
            DirContext ctx = new InitialDirContext(env);
            
            // Busca registros MX do domínio
            Attributes attrs = ctx.getAttributes(dominio, new String[]{"MX"});
            Attribute mxAttr = attrs.get("MX");
            
            if (mxAttr != null && mxAttr.size() > 0) {
                logger.debug("✅ Domínio {} possui {} registro(s) MX válido(s)", dominio, mxAttr.size());
                return true;
            }
            
            logger.warn("⚠️ Domínio {} não possui registros MX", dominio);
            return false;
            
        } catch (NamingException e) {
            // Domínio não existe ou não tem registros MX
            logger.warn("❌ Erro ao verificar domínio {}: {}", dominio, e.getMessage());
            return false;
        } catch (Exception e) {
            // Em caso de erro na verificação DNS, logamos mas permitimos o email
            // para não bloquear cadastros por problemas temporários de rede
            logger.error("❌ Erro inesperado ao verificar domínio {}: {}", dominio, e.getMessage());
            
            // Em produção, você pode querer retornar true aqui para não bloquear
            // cadastros por problemas temporários de DNS
            // Por segurança, vamos retornar false (bloqueia)
            return false;
        }
    }
}

