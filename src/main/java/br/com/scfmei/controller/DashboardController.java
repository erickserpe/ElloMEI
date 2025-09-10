package br.com.scfmei.controller;

import br.com.scfmei.service.ContaService;
import br.com.scfmei.service.DashboardService;
import br.com.scfmei.service.PessoaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    // Injetamos os outros serviços para buscar os dados para os filtros
    @Autowired
    private ContaService contaService;

    @Autowired
    private PessoaService pessoaService;

    @GetMapping("/")
    public String mostrarDashboard(
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(required = false) Long contaId,
            @RequestParam(required = false) Long pessoaId,
            Model model) {

        // 1. Busca os dados para preencher os dropdowns do filtro
        model.addAttribute("listaDeContas", contaService.buscarTodas());
        model.addAttribute("listaDePessoas", pessoaService.buscarTodas());

        // 2. Busca os totais (futuramente, eles usarão os filtros)
        model.addAttribute("saldoTotal", dashboardService.getSaldoTotal());
        model.addAttribute("totalEntradas", dashboardService.getTotalEntradasMesAtual());
        model.addAttribute("totalSaidas", dashboardService.getTotalSaidasMesAtual());

        // 3. Devolve os filtros selecionados para a view, para que eles permaneçam selecionados
        model.addAttribute("dataInicioSel", dataInicio);
        model.addAttribute("dataFimSel", dataFim);
        model.addAttribute("contaIdSel", contaId);
        model.addAttribute("pessoaIdSel", pessoaId);

        return "dashboard";
    }
}