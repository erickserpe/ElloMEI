package br.com.scfmei.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // "Carimbo" que diz ao Spring que esta é uma classe de configuração
public class WebConfig implements WebMvcConfigurer {

    // Pega o valor da propriedade 'file.upload-dir' do nosso application.properties
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Este método cria a "ponte" entre a URL e a pasta física

        // Mapeia a URL /uploads/qualquer-coisa para a pasta física ./uploads/
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir);
    }
}