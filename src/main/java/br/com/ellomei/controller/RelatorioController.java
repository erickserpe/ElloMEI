package br.com.ellomei.controller;

import br.com.ellomei.config.security.CurrentUser;
import br.com.ellomei.domain.*;
import br.com.ellomei.event.ReportGenerationRequestedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller responsável por gerenciar solicitações de relatórios.
 *
 * Este controller implementa o padrão de Eventos de Domínio para desacoplar
 * completamente a solicitação de relatórios da geração efetiva dos PDFs.
 *
 * Fluxo:
 * 1. Recebe requisição HTTP para gerar relatório
 * 2. Publica evento ReportGenerationRequestedEvent
 * 3. Retorna HTTP 202 (Accepted) imediatamente
 * 4. Listener processa o evento em background
 *
 * Benefícios:
 * - Resposta HTTP rápida (não espera geração do PDF)
 * - Escalabilidade (geração em background)
 * - Resiliência (falhas não afetam resposta HTTP)
 * - Flexibilidade (múltiplos listeners podem reagir)
 *
 * @author ElloMEI Team
 * @since 1.0.0
 */
@Controller
@RequestMapping("/relatorios")
public class RelatorioController {

    @Autowired
    private ApplicationEventPublisher eventPublisher;


    /**
     * Solicita geração de relatório de faturamento dinâmico.
     *
     * Este endpoint publica um evento e retorna imediatamente com HTTP 202 (Accepted).
     * O PDF será gerado em background e o usuário será notificado quando estiver pronto.
     *
     * @return ResponseEntity com status 202 e mensagem informativa
     */
    @GetMapping("/faturamento/dinamico/pdf")
    public ResponseEntity<Map<String, String>> gerarRelatorioFaturamentoDinamico(
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
            @CurrentUser Usuario usuario) {

        // Publica evento para geração assíncrona do relatório
        ReportGenerationRequestedEvent event = ReportGenerationRequestedEvent.builder()
                .source(this)
                .tipoRelatorio("FATURAMENTO_DINAMICO")
                .tipoVisao(tipoVisao)
                .dataInicio(dataInicio)
                .dataFim(dataFim)
                .contaId(contaId)
                .contatoId(contatoId)
                .tipo(tipo)
                .categoriaId(categoriaId)
                .comNotaFiscal(comNotaFiscal)
                .descricao(descricao)
                .status(status)
                .usuario(usuario)
                .build();

        eventPublisher.publishEvent(event);

        // Retorna resposta imediata
        Map<String, String> response = new HashMap<>();
        response.put("message", "Seu relatório está sendo processado. Você será notificado quando estiver pronto.");
        response.put("status", "PROCESSING");
        response.put("tipoRelatorio", "FATURAMENTO_DINAMICO");
        response.put("tipoVisao", tipoVisao);

        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    /**
     * Solicita geração de relatório de compras com nota fiscal.
     *
     * Este endpoint publica um evento e retorna imediatamente com HTTP 202 (Accepted).
     * O PDF será gerado em background e o usuário será notificado quando estiver pronto.
     *
     * @return ResponseEntity com status 202 e mensagem informativa
     */
    @GetMapping("/compras-com-nota/pdf")
    public ResponseEntity<Map<String, String>> gerarRelatorioComprasNota(
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(required = false) Long contaId,
            @RequestParam(required = false) Long contatoId,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) StatusLancamento status,
            @CurrentUser Usuario usuario) {

        // Publica evento para geração assíncrona do relatório
        ReportGenerationRequestedEvent event = ReportGenerationRequestedEvent.builder()
                .source(this)
                .tipoRelatorio("COMPRAS_COM_NOTA")
                .dataInicio(dataInicio)
                .dataFim(dataFim)
                .contaId(contaId)
                .contatoId(contatoId)
                .categoriaId(categoriaId)
                .descricao(descricao)
                .status(status)
                .usuario(usuario)
                .build();

        eventPublisher.publishEvent(event);

        // Retorna resposta imediata
        Map<String, String> response = new HashMap<>();
        response.put("message", "Seu relatório está sendo processado. Você será notificado quando estiver pronto.");
        response.put("status", "PROCESSING");
        response.put("tipoRelatorio", "COMPRAS_COM_NOTA");

        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    /**
     * Solicita geração de relatório de lançamentos.
     *
     * Este endpoint publica um evento e retorna imediatamente com HTTP 202 (Accepted).
     * O PDF será gerado em background e o usuário será notificado quando estiver pronto.
     *
     * @return ResponseEntity com status 202 e mensagem informativa
     */
    @GetMapping("/lancamentos/pdf")
    public ResponseEntity<Map<String, String>> gerarRelatorioLancamentos(
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(required = false) Long contaId,
            @RequestParam(required = false) Long contatoId,
            @RequestParam(required = false) TipoLancamento tipo,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Boolean comNotaFiscal,
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) StatusLancamento status,
            @CurrentUser Usuario usuario) {

        // Publica evento para geração assíncrona do relatório
        ReportGenerationRequestedEvent event = ReportGenerationRequestedEvent.builder()
                .source(this)
                .tipoRelatorio("LANCAMENTOS")
                .dataInicio(dataInicio)
                .dataFim(dataFim)
                .contaId(contaId)
                .contatoId(contatoId)
                .tipo(tipo)
                .categoriaId(categoriaId)
                .comNotaFiscal(comNotaFiscal)
                .descricao(descricao)
                .status(status)
                .usuario(usuario)
                .build();

        eventPublisher.publishEvent(event);

        // Retorna resposta imediata
        Map<String, String> response = new HashMap<>();
        response.put("message", "Seu relatório está sendo processado. Você será notificado quando estiver pronto.");
        response.put("status", "PROCESSING");
        response.put("tipoRelatorio", "LANCAMENTOS");

        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }
}