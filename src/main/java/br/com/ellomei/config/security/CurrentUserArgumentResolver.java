package br.com.ellomei.config.security;

import br.com.ellomei.domain.Usuario;
import br.com.ellomei.repository.UsuarioRepository;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolver customizado para injetar automaticamente o usuário logado nos métodos dos controllers.
 * 
 * Este resolver identifica parâmetros anotados com @CurrentUser e resolve o Principal
 * do SecurityContext para a entidade Usuario do banco de dados.
 */
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final UsuarioRepository usuarioRepository;

    public CurrentUserArgumentResolver(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Verifica se este resolver suporta o parâmetro.
     * Retorna true se o parâmetro é do tipo Usuario e tem a anotação @CurrentUser.
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(Usuario.class) 
            && parameter.hasParameterAnnotation(CurrentUser.class);
    }

    /**
     * Resolve o argumento buscando o usuário logado no banco de dados.
     * Usa o username do SecurityContext para buscar a entidade Usuario.
     */
    @Override
    public Object resolveArgument(
            MethodParameter parameter, 
            ModelAndViewContainer mavContainer, 
            NativeWebRequest webRequest, 
            WebDataBinderFactory binderFactory) throws Exception {
        
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Usuário logado não encontrado no resolver."));
    }
}

