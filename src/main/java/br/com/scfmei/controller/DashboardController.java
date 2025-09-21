package br.com.scfmei.controller;

import br.com.scfmei.domain.Lancamento;
import br.com.scfmei.domain.TipoLancamento;
import br.com.scfmei.service.CategoriaDespesaService;
import br.com.scfmei.service.ContaService;
import br.com.scfmei.service.DashboardService;
import br.com.scfmei.service.ContatoService;
import br.com.scfmei.service.LancamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import br.com.scfmei.domain.StatusLancamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private ContaService contaService;

    @Autowired
    private ContatoService contatoService;

    @Autowired
    private CategoriaDespesaService categoriaService;

    @Autowired
    private LancamentoService lancamentoService; // <-- Faltava essa injeção

    @GetMapping("/")
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
            Model model) {

        // 1. Busca dados para os dropdowns dos filtros
        model.addAttribute("listaDeContas", contaService.buscarTodas());
        model.addAttribute("listaDePessoas", contatoService.buscarTodos()); // Corrigido aqui
        model.addAttribute("listaDeCategorias", categoriaService.buscarTodas());

        // 2. Busca os lançamentos com filtros aplicados
        List<Lancamento> lancamentosFiltrados = lancamentoService.buscarComFiltros(
                dataInicio, dataFim, contaId, contatoId, tipo, categoriaId, comNotaFiscal, descricao, status
        );

        // 3. Calcula KPIs
        BigDecimal totalEntradas = lancamentosFiltrados.stream()
                .filter(l -> l.getTipo() == TipoLancamento.ENTRADA)
                .map(Lancamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSaidas = lancamentosFiltrados.stream()
                .filter(l -> l.getTipo() == TipoLancamento.SAIDA)
                .map(Lancamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("saldoTotal", dashboardService.getSaldoTotal(contaId));
        model.addAttribute("totalEntradas", totalEntradas);
        model.addAttribute("totalSaidas", totalSaidas);

        // 4. Devolve filtros selecionados para a view
        model.addAttribute("dataInicioSel", dataInicio);
        model.addAttribute("dataFimSel", dataFim);
        model.addAttribute("contaIdSel", contaId);
        model.addAttribute("contatoIdSel", contatoId);
        model.addAttribute("tipoSel", tipo);
        model.addAttribute("categoriaIdSel", categoriaId);
        model.addAttribute("comNotaFiscalSel", comNotaFiscal);
        model.addAttribute("descricaoSel", descricao);

        return "dashboard";
    }
}
