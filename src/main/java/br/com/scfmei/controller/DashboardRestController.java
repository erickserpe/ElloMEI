// src/main/java/br/com/scfmei/controller/DashboardRestController.java
package br.com.scfmei.controller;

import br.com.scfmei.domain.ChartData;
import br.com.scfmei.domain.Usuario;
import br.com.scfmei.repository.UsuarioRepository;
import br.com.scfmei.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardRestController {

    @Autowired private DashboardService dashboardService;
    @Autowired private UsuarioRepository usuarioRepository;

    private Usuario getUsuarioLogado(Principal principal) {
        return usuarioRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Usuário logado não encontrado."));
    }

    @GetMapping("/despesas-por-categoria")
    public ResponseEntity<List<ChartData>> getDespesasPorCategoria(
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(required = false) Long contaId,
            @RequestParam(required = false) Long contatoId,
            Principal principal) { // Pega o usuário logado

        Usuario usuario = getUsuarioLogado(principal);
        List<ChartData> data = dashboardService.getDespesasPorCategoria(dataInicio, dataFim, contaId, contatoId, usuario);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/fluxo-caixa-mensal")
    public ResponseEntity<Map<String, List<?>>> getFluxoDeCaixaMensal(Principal principal) {
        Usuario usuario = getUsuarioLogado(principal);
        Map<String, List<?>> data = dashboardService.getFluxoDeCaixaUltimos12Meses(usuario);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/faturamento-widget")
    public ResponseEntity<Map<String, BigDecimal>> getDadosWidgetFaturamento(@RequestParam String tipoCalculo, Principal principal) {
        Usuario usuario = getUsuarioLogado(principal);
        YearMonth mesAtual = YearMonth.now();
        int anoAtual = mesAtual.getYear();

        BigDecimal faturamentoAnual;
        BigDecimal faturamentoMensal;

        switch (tipoCalculo) {
            case "BANCARIO":
                faturamentoAnual = dashboardService.getFaturamentoBancario(anoAtual, usuario);
                faturamentoMensal = dashboardService.getFaturamentoBancarioMesAtual(usuario);
                break;
            case "ESTIMADO_CUSTOS":
                faturamentoAnual = dashboardService.getMetaFaturamentoBaseadoEmCustos(anoAtual, usuario);
                faturamentoMensal = dashboardService.getMetaFaturamentoBaseadoEmCustosMensal(usuario);
                break;
            default: // "OFICIAL"
                faturamentoAnual = dashboardService.getFaturamentoOficial(anoAtual, usuario);
                faturamentoMensal = dashboardService.getTotalEntradas(mesAtual.atDay(1), mesAtual.atEndOfMonth(), null, null, usuario);
                break;
        }

        Map<String, BigDecimal> response = new HashMap<>();
        response.put("faturamentoAnual", faturamentoAnual);
        response.put("faturamentoMensal", faturamentoMensal);

        return ResponseEntity.ok(response);
    }
}