package br.com.ellomei.config;

import br.com.ellomei.domain.Usuario;
import br.com.ellomei.repository.UsuarioRepository;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Aspecto Spring AOP que ativa automaticamente o filtro de multi-tenancy (tenantFilter)
 * para todas as requisições aos controllers.
 * 
 * Este aspecto garante que cada usuário veja apenas seus próprios dados,
 * aplicando o filtro Hibernate com o ID do usuário autenticado.
 * 
 * @author SCF-MEI Team
 * @since 1.0.0
 */
@Aspect
@Component
public class TenantFilterAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Intercepta todas as chamadas aos controllers e ativa o filtro de tenant.
     * 
     * O filtro é ativado apenas para usuários autenticados (não anônimos),
     * garantindo que todas as queries do Hibernate incluam automaticamente
     * a condição WHERE usuario_id = :tenantId.
     * 
     * Pointcut: execution(* br.com.scfmei.controller.*.*(..))
     * - Intercepta todos os métodos de todos os controllers
     */
    @Before("execution(* br.com.scfmei.controller.*.*(..))")
    public void activateTenantFilter() {
        final Session session = entityManager.unwrap(Session.class);
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            usuarioRepository.findByUsername(username).ifPresent(usuario -> {
                session.enableFilter("tenantFilter").setParameter("tenantId", usuario.getId());
            });
        }
    }
}

