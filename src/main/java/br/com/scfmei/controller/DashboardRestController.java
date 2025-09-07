package br.com.scfmei.controller;

import br.com.scfmei.domain.ChartData;
import br.com.scfmei.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard") // Todos os endpoints aqui começarão com /api/dashboard
public class DashboardRestController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/despesas-por-categoria")
    public ResponseEntity<List<ChartData>> getDespesasPorCategoria() {
        List<ChartData> data = dashboardService.getDespesasPorCategoriaMesAtual();
        return ResponseEntity.ok(data);
    }
}