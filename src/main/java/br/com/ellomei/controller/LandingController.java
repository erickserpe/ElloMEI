package br.com.ellomei.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller para a página de landing/home do Ello MEI
 * Responsável por exibir a página inicial com design glassmorphism
 */
@Controller
public class LandingController {

    /**
     * Exibe a página de landing principal para usuários não autenticados
     * Redireciona usuários autenticados para o dashboard
     * @param authentication objeto de autenticação do Spring Security
     * @return template da landing page ou redirecionamento
     */
    @GetMapping("/")
    public String landing(Authentication authentication) {
        // Se o usuário já está autenticado, redireciona para o dashboard
        if (authentication != null && authentication.isAuthenticated() &&
            !authentication.getName().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        // Caso contrário, mostra a landing page
        return "landing";
    }

    /**
     * Rota alternativa para a landing page
     * @return template da landing page
     */
    @GetMapping("/home")
    public String home() {
        return "landing";
    }

    /**
     * Exibe a página de demonstração do design glassmorphism
     * @return template da demo page
     */
    @GetMapping("/demo")
    public String demo() {
        return "demo";
    }
}
