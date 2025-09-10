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

        // 1. Busca os dados para preencher os dropdowns do filtro (como antes)
        model.addAttribute("listaDeContas", contaService.buscarTodas());
        model.addAttribute("listaDePessoas", pessoaService.buscarTodas());

        // 2. AGORA, PASSA OS FILTROS PARA O SERVIÇO FAZER OS CÁLCULOS
        model.addAttribute("saldoTotal", dashboardService.getSaldoTotal(contaId));
        model.addAttribute("totalEntradas", dashboardService.getTotalEntradas(dataInicio, dataFim, contaId, pessoaId));
        model.addAttribute("totalSaidas", dashboardService.getTotalSaidas(dataInicio, dataFim, contaId, pessoaId));

        // Devolve os filtros selecionados para a view (como antes)
        model.addAttribute("dataInicioSel", dataInicio);
        model.addAttribute("dataFimSel", dataFim);
        model.addAttribute("contaIdSel", contaId);
        model.addAttribute("pessoaIdSel", pessoaId);

        return "dashboard";
    }

}