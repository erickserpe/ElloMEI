package br.com.scfmei.controller;

import br.com.scfmei.domain.*;
import br.com.scfmei.repository.UsuarioRepository;
import br.com.scfmei.service.DashboardService;
import br.com.scfmei.service.LancamentoService;
import br.com.scfmei.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Controller
@RequestMapping("/relatorios")
public class RelatorioController {

    @Autowired private LancamentoService lancamentoService;
    @Autowired private PdfService pdfService;
    @Autowired private DashboardService dashboardService;


    @Autowired private UsuarioRepository usuarioRepository;

    private Usuario getUsuarioLogado(Principal principal) {
        return usuarioRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Usuário logado não encontrado."));
    }


    @GetMapping("/faturamento/dinamico/pdf")
    public ResponseEntity<byte[]> gerarRelatorioFaturamentoDinamico(
            @RequestParam String tipoVisao,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(required = false) Long contaId,
            @RequestParam(required = false) Long contatoId,
            @RequestParam(required = false) TipoLancamento tipo,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Boolean comNotaFiscal,
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) StatusLancamento status,
            Principal principal) throws Exception { // Added throws Exception for async handling

        Usuario usuario = getUsuarioLogado(principal);
        Map<String, Object> variaveis = new HashMap<>();
        String templateNome;
        List<Lancamento> lancamentos;

        if ("OFICIAL".equals(tipoVisao) || "BANCARIO".equals(tipoVisao) || "ESTIMADO_CUSTOS".equals(tipoVisao)) {

            templateNome = "relatorio_faturamento_mei";
            int anoAtual = (dataFim != null) ? dataFim.getYear() : LocalDate.now().getYear();
            String tituloVisao = "";

            if ("ESTIMADO_CUSTOS".equals(tipoVisao)) {
                tituloVisao = "Meta (Baseado em Compras)";

                BigDecimal faturamentoAnual = dashboardService.getMetaFaturamentoBaseadoEmCustos(anoAtual, usuario);
                lancamentos = lancamentoService.buscarComFiltros(dataInicio, dataFim, contaId, contatoId, TipoLancamento.SAIDA, categoriaId, true, descricao, status, usuario);
                variaveis.put("faturamentoAnual", faturamentoAnual);

            } else {
                tituloVisao = "OFICIAL".equals(tipoVisao) ? "Faturamento Oficial" : "Faturamento Bancário";

                BigDecimal faturamentoAnual = dashboardService.getFaturamentoOficial(anoAtual, usuario);
                lancamentos = lancamentoService.buscarComFiltros(dataInicio, dataFim, contaId, contatoId, TipoLancamento.ENTRADA, categoriaId, comNotaFiscal, descricao, status, usuario);
                variaveis.put("faturamentoAnual", faturamentoAnual);
            }

            BigDecimal totalPeriodo = lancamentos.stream()
                    .map(Lancamento::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal faturamentoAnual = (BigDecimal) variaveis.get("faturamentoAnual");
            BigDecimal limiteAnual = new BigDecimal("81000");
            BigDecimal limiteMensal = limiteAnual.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
            BigDecimal percentualAnual = faturamentoAnual.divide(limiteAnual, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            BigDecimal faturamentoMensal = faturamentoAnual.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
            BigDecimal percentualMensal = faturamentoMensal.divide(limiteMensal, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

            variaveis.put("tituloVisao", tituloVisao);
            variaveis.put("percentualAnual", percentualAnual);
            variaveis.put("faturamentoMensal", faturamentoMensal);
            variaveis.put("percentualMensal", percentualMensal);
            variaveis.put("totalPeriodo", totalPeriodo);

        } else {
            templateNome = "relatorio_lancamentos";

            lancamentos = lancamentoService.buscarComFiltros(dataInicio, dataFim, contaId, contatoId, tipo, categoriaId, comNotaFiscal, descricao, status, usuario);
            BigDecimal total = lancamentos.stream().map(Lancamento::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
            variaveis.put("total", total);
        }

        variaveis.put("lancamentos", lancamentos);
        variaveis.put("dataInicio", dataInicio);
        variaveis.put("dataFim", dataFim);

        CompletableFuture<byte[]> pdfFuture = pdfService.gerarPdfDeHtml(templateNome, variaveis);
        byte[] pdfBytes = pdfFuture.get(); // .get() waits for the background task to complete

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "relatorio_" + tipoVisao.toLowerCase() + ".pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/compras-com-nota/pdf")
    public ResponseEntity<byte[]> gerarRelatorioComprasNota(
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(required = false) Long contaId,
            @RequestParam(required = false) Long contatoId,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) StatusLancamento status,
            Principal principal) throws Exception {

        Usuario usuario = getUsuarioLogado(principal);

        // Filter specifically for expenses (SAIDA) with an invoice (comNotaFiscal = true)
        List<Lancamento> lancamentos = lancamentoService.buscarComFiltros(
                dataInicio, dataFim, contaId, contatoId, TipoLancamento.SAIDA,
                categoriaId, true, descricao, status, usuario);

        BigDecimal total = lancamentos.stream()
                .map(Lancamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> variaveis = new HashMap<>();
        variaveis.put("lancamentos", lancamentos);
        variaveis.put("total", total);
        variaveis.put("dataInicio", dataInicio);
        variaveis.put("dataFim", dataFim);

        CompletableFuture<byte[]> pdfFuture = pdfService.gerarPdfDeHtml("relatorio_compras_com_nota", variaveis);
        byte[] pdfBytes = pdfFuture.get(); // .get() waits for the background task to complete

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "relatorio_compras_com_nota.pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/lancamentos/pdf")
    public ResponseEntity<byte[]> gerarRelatorioLancamentos(
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(required = false) Long contaId,
            @RequestParam(required = false) Long contatoId,
            @RequestParam(required = false) TipoLancamento tipo,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Boolean comNotaFiscal,
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) StatusLancamento status,
            Principal principal) throws Exception {

        Usuario usuario = getUsuarioLogado(principal);

        // Fetch all transactions based on filters without forcing type or invoice status
        List<Lancamento> lancamentos = lancamentoService.buscarComFiltros(
                dataInicio, dataFim, contaId, contatoId, tipo,
                categoriaId, comNotaFiscal, descricao, status, usuario);

        // Prepare variables for the PDF template
        Map<String, Object> variaveis = new HashMap<>();
        variaveis.put("lancamentos", lancamentos);
        variaveis.put("dataInicio", dataInicio);
        variaveis.put("dataFim", dataFim);

        // Process file paths to be accessible in the PDF
        for (Lancamento lancamento : lancamentos) {
            for (Comprovante comprovante : lancamento.getComprovantes()) {
                // Ensure the path starts with /uploads/ for PDF generation
                String currentPath = comprovante.getPathArquivo();
                if (currentPath != null && !currentPath.startsWith("/uploads/")) {
                    comprovante.setPathArquivo("/uploads/" + currentPath);
                }
            }
        }

        CompletableFuture<byte[]> pdfFuture = pdfService.gerarPdfDeHtml("relatorio_lancamentos", variaveis);
        byte[] pdfBytes = pdfFuture.get(); // .get() waits for the background task to complete

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "relatorio_lancamentos_detalhados.pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}