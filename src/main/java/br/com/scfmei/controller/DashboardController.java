package br.com.scfmei.controller;

import br.com.scfmei.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/") // Mapeia para a página inicial da aplicação
    public String mostrarDashboard(Model model) {
        model.addAttribute("saldoTotal", dashboardService.getSaldoTotal());
        model.addAttribute("totalEntradas", dashboardService.getTotalEntradasMesAtual());
        model.addAttribute("totalSaidas", dashboardService.getTotalSaidasMesAtual());
        return "dashboard"; // Retorna o arquivo dashboard.html
    }
}