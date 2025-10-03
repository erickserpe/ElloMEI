package br.com.scfmei.controller;

import br.com.scfmei.domain.Usuario;
import br.com.scfmei.dto.UsageMetricsDTO;
import br.com.scfmei.repository.UsuarioRepository;
import br.com.scfmei.service.UsageMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;

/**
 * Controller responsável pelo dashboard de uso do usuário.
 * 
 * Endpoints:
 * - GET /uso - Página do dashboard de uso
 * - GET /api/uso/metricas - API JSON com métricas de uso
 * 
 * @author SCF-MEI Team
 * @since 1.0.0
 */
@Controller
@RequestMapping("/uso")
public class UsageDashboardController {
    
    @Autowired
    private UsageMetricsService usageMetricsService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    /**
     * Obtém o usuário logado.
     */
    private Usuario getUsuarioLogado(Principal principal) {
        return usuarioRepository.findByUsername(principal.getName())
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
    
    /**
     * Página do dashboard de uso.
     */
    @GetMapping
    public String paginaDashboard(Model model, Principal principal) {
        Usuario usuario = getUsuarioLogado(principal);
        UsageMetricsDTO metrics = usageMetricsService.calcularMetricas(usuario);
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("metrics", metrics);
        
        return "uso/dashboard";
    }
    
    /**
     * API JSON para obter métricas de uso.
     * 
     * Útil para atualizar o dashboard via AJAX sem recarregar a página.
     */
    @GetMapping("/api/metricas")
    @ResponseBody
    public UsageMetricsDTO getMetricas(Principal principal) {
        Usuario usuario = getUsuarioLogado(principal);
        return usageMetricsService.calcularMetricas(usuario);
    }
}

