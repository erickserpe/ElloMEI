package br.com.ellomei.controller;

import br.com.ellomei.config.security.CurrentUser;
import br.com.ellomei.domain.Usuario;
import br.com.ellomei.dto.UsageMetricsDTO;
import br.com.ellomei.service.UsageMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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

    /**
     * Página do dashboard de uso.
     */
    @GetMapping
    public String paginaDashboard(Model model, @CurrentUser Usuario usuario) {
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
    public UsageMetricsDTO getMetricas(@CurrentUser Usuario usuario) {
        return usageMetricsService.calcularMetricas(usuario);
    }
}

