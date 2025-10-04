package br.com.scfmei.config;

import br.com.scfmei.config.security.CurrentUserArgumentResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Pega o valor da propriedade 'file.upload-dir' do application.properties
    @Value("${file.upload-dir}")
    private String uploadDir;

    private final CurrentUserArgumentResolver currentUserArgumentResolver;

    public WebConfig(CurrentUserArgumentResolver currentUserArgumentResolver) {
        this.currentUserArgumentResolver = currentUserArgumentResolver;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // método ponte entre a URL e a pasta física

        // Mapeia a URL /uploads/qualquer-coisa para a pasta física ./uploads/
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir);
    }

    /**
     * Registra o resolver customizado para injeção automática do usuário logado.
     * Permite usar @CurrentUser Usuario usuario nos métodos dos controllers.
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }
}