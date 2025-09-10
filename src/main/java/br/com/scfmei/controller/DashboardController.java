package br.com.scfmei.controller;

import br.com.scfmei.service.ContaService;
import br.com.scfmei.service.DashboardService;
import br.com.scfmei.service.PessoaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import br.com.scfmei.domain.Lancamento;
import br.com.scfmei.service.LancamentoService;
import br.com.scfmei.service.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private PdfService pdfService;

    @Autowired
    private LancamentoService lancamentoService;

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
    @GetMapping("/relatorio/pdf")
    public ResponseEntity<byte[]> gerarRelatorioPdf(
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(required = false) Long contaId,
            @RequestParam(required = false) Long pessoaId) {

        // 1. Busca os dados já filtrados (reaproveitando nossa lógica!)
        List<Lancamento> lancamentos = lancamentoService.buscarComFiltros(dataInicio, dataFim, contaId, pessoaId);

        // 2. Prepara as variáveis para enviar para o template HTML
        Map<String, Object> variaveis = new HashMap<>();
        variaveis.put("lancamentos", lancamentos);
        variaveis.put("dataInicio", dataInicio);
        variaveis.put("dataFim", dataFim);

        // 3. Chama o serviço para gerar o PDF a partir do template
        byte[] pdfBytes = pdfService.gerarPdfDeHtml("relatorio_lancamentos", variaveis);

        // 4. Prepara a resposta para o navegador forçar o download
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "relatorio_lancamentos.pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

}