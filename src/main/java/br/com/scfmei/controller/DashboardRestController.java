package br.com.scfmei.controller;

import br.com.scfmei.domain.ChartData;
import br.com.scfmei.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardRestController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/despesas-por-categoria")
    public ResponseEntity<List<ChartData>> getDespesasPorCategoria(
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(required = false) Long contaId,
            @RequestParam(required = false) Long pessoaId) {

        // Chama o novo método do serviço, passando os filtros recebidos da URL
        List<ChartData> data = dashboardService.getDespesasPorCategoria(dataInicio, dataFim, contaId, pessoaId);
        return ResponseEntity.ok(data);
    }
}