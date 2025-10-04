package br.com.scfmei.config.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * Anotação customizada para injetar automaticamente o usuário logado nos métodos dos controllers.
 * 
 * Uso:
 * <pre>
 * {@code
 * @GetMapping
 * public String metodo(@CurrentUser Usuario usuario) {
 *     // usuario já está disponível aqui
 * }
 * }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal
public @interface CurrentUser {
}

