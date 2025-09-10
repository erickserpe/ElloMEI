package br.com.scfmei.controller;

import br.com.scfmei.domain.ChartData;
import br.com.scfmei.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;
import java.time.YearMonth;

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
    @GetMapping("/faturamento-widget")
    public ResponseEntity<Map<String, BigDecimal>> getDadosWidgetFaturamento(@RequestParam String tipoCalculo) {
        YearMonth mesAtual = YearMonth.now();
        int anoAtual = mesAtual.getYear();

        BigDecimal faturamentoAnual;
        BigDecimal faturamentoMensal;

        switch (tipoCalculo) {
            case "BANCARIO":
                faturamentoAnual = dashboardService.getFaturamentoBancario(anoAtual);
                faturamentoMensal = dashboardService.getFaturamentoBancarioMesAtual(); // Precisamos criar este método
                break;
            case "ESTIMADO_CUSTOS":
                faturamentoAnual = dashboardService.getMetaFaturamentoBaseadoEmCustos(anoAtual);
                faturamentoMensal = dashboardService.getMetaFaturamentoBaseadoEmCustosMensal(); // E este também
                break;
            default: // "OFICIAL"
                faturamentoAnual = dashboardService.getFaturamentoOficial(anoAtual);
                faturamentoMensal = dashboardService.getFaturamentoMesAtual();
                break;
        }

        Map<String, BigDecimal> response = new HashMap<>();
        response.put("faturamentoAnual", faturamentoAnual);
        response.put("faturamentoMensal", faturamentoMensal);

        return ResponseEntity.ok(response);
    }
}