package br.com.scfmei.controller;

import br.com.scfmei.domain.Lancamento;
import br.com.scfmei.domain.TipoLancamento;
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
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/relatorios")
public class RelatorioController {

    @Autowired private LancamentoService lancamentoService;
    @Autowired private PdfService pdfService;

    @GetMapping("/faturamento/dinamico/pdf")
    public ResponseEntity<byte[]> gerarRelatorioFaturamentoDinamico(
            @RequestParam String tipoVisao,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim) {

        Map<String, Object> variaveis = new HashMap<>();
        String templateNome = "";
        List<Lancamento> lancamentos;

        switch (tipoVisao) {
            case "OFICIAL":
            case "BANCARIO":
                lancamentos = lancamentoService.buscarComFiltros(dataInicio, dataFim, null, null, TipoLancamento.ENTRADA, null);
                templateNome = "relatorio_entradas";
                variaveis.put("tituloVisao", tipoVisao);
                break;
            case "ESTIMADO_CUSTOS":
                lancamentos = lancamentoService.buscarComFiltros(dataInicio, dataFim, null, null, TipoLancamento.SAIDA, true);
                templateNome = "relatorio_compras_com_nota";
                break;
            default:
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        BigDecimal total = lancamentos.stream().map(Lancamento::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
        variaveis.put("lancamentos", lancamentos);
        variaveis.put("total", total);
        variaveis.put("dataInicio", dataInicio);
        variaveis.put("dataFim", dataFim);

        byte[] pdfBytes = pdfService.gerarPdfDeHtml(templateNome, variaveis);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "relatorio_" + tipoVisao.toLowerCase() + ".pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}