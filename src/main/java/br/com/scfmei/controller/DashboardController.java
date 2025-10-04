package br.com.scfmei.controller;

import br.com.scfmei.config.security.CurrentUser;
import br.com.scfmei.domain.*;
import br.com.scfmei.dto.UsageMetricsDTO;
import br.com.scfmei.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class DashboardController {

    @Autowired private DashboardService dashboardService;
    @Autowired private ContaService contaService;
    @Autowired private ContatoService contatoService;
    @Autowired private CategoriaDespesaService categoriaService;
    @Autowired private LancamentoService lancamentoService;
    @Autowired private UsageMetricsService usageMetricsService;

    @GetMapping("/dashboard")
    public String mostrarDashboard(
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(required = false) Long contaId,
            @RequestParam(required = false) Long contatoId,
            @RequestParam(required = false) TipoLancamento tipo,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Boolean comNotaFiscal,
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) StatusLancamento status,
            @CurrentUser Usuario usuario,
            Model model) {

        // 1. Busca dados para os dropdowns dos filtros
        model.addAttribute("listaDeContas", contaService.buscarTodasPorUsuario(usuario));
        model.addAttribute("listaDePessoas", contatoService.buscarTodosPorUsuario(usuario));
        model.addAttribute("listaDeCategorias", categoriaService.buscarTodasPorUsuario(usuario));

        // 2. Busca os lançamentos com filtros aplicados
        List<Lancamento> lancamentosFiltrados = lancamentoService.buscarComFiltros(
                dataInicio, dataFim, contaId, contatoId, tipo, categoriaId, comNotaFiscal, descricao, status, usuario
        );

        // 3. Calcula KPIs
        BigDecimal totalEntradas = lancamentosFiltrados.stream()
                .filter(l -> l.getTipo() == TipoLancamento.ENTRADA && l.getStatus() == StatusLancamento.PAGO)
                .map(Lancamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSaidas = lancamentosFiltrados.stream()
                .filter(l -> l.getTipo() == TipoLancamento.SAIDA && l.getStatus() == StatusLancamento.PAGO)
                .map(Lancamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("saldoTotal", dashboardService.getSaldoTotal(usuario, contaId));
        model.addAttribute("totalEntradas", totalEntradas);
        model.addAttribute("totalSaidas", totalSaidas);

        // 3.5. Adiciona métricas de uso do plano
        UsageMetricsDTO usageMetrics = usageMetricsService.calcularMetricas(usuario);
        model.addAttribute("usageMetrics", usageMetrics);

        // 4. Devolve filtros selecionados para a view (completo)
        model.addAttribute("dataInicioSel", dataInicio);
        model.addAttribute("dataFimSel", dataFim);
        model.addAttribute("contaIdSel", contaId);
        model.addAttribute("contatoIdSel", contatoId);
        model.addAttribute("tipoSel", tipo);
        model.addAttribute("categoriaIdSel", categoriaId);
        model.addAttribute("comNotaFiscalSel", comNotaFiscal);
        model.addAttribute("descricaoSel", descricao);
        model.addAttribute("statusSel", status);

        return "dashboard";
    }
}