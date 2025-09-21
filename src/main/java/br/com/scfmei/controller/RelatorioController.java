package br.com.scfmei.controller;

import br.com.scfmei.domain.Lancamento;
import br.com.scfmei.domain.TipoLancamento;
import br.com.scfmei.service.DashboardService;
import br.com.scfmei.service.LancamentoService;
import br.com.scfmei.domain.StatusLancamento;
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
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/relatorios")
public class RelatorioController {

    @Autowired private LancamentoService lancamentoService;
    @Autowired private PdfService pdfService;
    @Autowired private DashboardService dashboardService;

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
            @RequestParam(required = false) StatusLancamento status) {

        Map<String, Object> variaveis = new HashMap<>();
        String templateNome;
        List<Lancamento> lancamentos;

        if ("OFICIAL".equals(tipoVisao) || "BANCARIO".equals(tipoVisao) || "ESTIMADO_CUSTOS".equals(tipoVisao)) {

            templateNome = "relatorio_faturamento_mei";
            int anoAtual = (dataFim != null) ? dataFim.getYear() : LocalDate.now().getYear();

            BigDecimal faturamentoAnual = BigDecimal.ZERO;
            String tituloVisao = "";

            if ("ESTIMADO_CUSTOS".equals(tipoVisao)) {
                tituloVisao = "Meta (Baseado em Compras)";
                faturamentoAnual = dashboardService.getMetaFaturamentoBaseadoEmCustos(anoAtual);
                // CORREÇÃO AQUI: Adicionado o parâmetro 'status'
                lancamentos = lancamentoService.buscarComFiltros(
                        dataInicio, dataFim, contaId, contatoId, TipoLancamento.SAIDA, categoriaId, true, descricao, status);
            } else { // OFICIAL ou BANCÁRIO
                tituloVisao = "OFICIAL".equals(tipoVisao) ? "Faturamento Oficial" : "Faturamento Bancário";
                faturamentoAnual = dashboardService.getFaturamentoOficial(anoAtual);
                // CORREÇÃO AQUI: Adicionado o parâmetro 'status'
                lancamentos = lancamentoService.buscarComFiltros(
                        dataInicio, dataFim, contaId, contatoId, TipoLancamento.ENTRADA, categoriaId, comNotaFiscal, descricao, status);
            }

            BigDecimal totalPeriodo = lancamentos.stream()
                    .map(Lancamento::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal limiteAnual = new BigDecimal("81000");
            BigDecimal limiteMensal = limiteAnual.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);

            BigDecimal percentualAnual = faturamentoAnual
                    .divide(limiteAnual, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            BigDecimal faturamentoMensal = faturamentoAnual.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);

            BigDecimal percentualMensal = faturamentoMensal
                    .divide(limiteMensal, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            variaveis.put("tituloVisao", tituloVisao);
            variaveis.put("faturamentoAnual", faturamentoAnual);
            variaveis.put("percentualAnual", percentualAnual);
            variaveis.put("faturamentoMensal", faturamentoMensal);
            variaveis.put("percentualMensal", percentualMensal);
            variaveis.put("totalPeriodo", totalPeriodo);

        } else { // Lógica para relatório genérico
            templateNome = "relatorio_lancamentos";
            // CORREÇÃO AQUI: Adicionado o parâmetro 'status'
            lancamentos = lancamentoService.buscarComFiltros(dataInicio, dataFim, contaId, contatoId, tipo, categoriaId, comNotaFiscal, descricao, status);
            BigDecimal total = lancamentos.stream().map(Lancamento::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
            variaveis.put("total", total);
        }

        variaveis.put("lancamentos", lancamentos);
        variaveis.put("dataInicio", dataInicio);
        variaveis.put("dataFim", dataFim);

        byte[] pdfBytes = pdfService.gerarPdfDeHtml(templateNome, variaveis);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "relatorio_" + tipoVisao.toLowerCase() + ".pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
