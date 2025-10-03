package br.com.scfmei.listener;

import br.com.scfmei.domain.Comprovante;
import br.com.scfmei.domain.Lancamento;
import br.com.scfmei.domain.TipoLancamento;
import br.com.scfmei.domain.Usuario;
import br.com.scfmei.event.ReportGenerationRequestedEvent;
import br.com.scfmei.service.DashboardService;
import br.com.scfmei.service.LancamentoService;
import br.com.scfmei.service.PdfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Listener para eventos de solicitação de geração de relatórios.
 * 
 * Este componente escuta eventos ReportGenerationRequestedEvent e processa
 * a geração de PDFs em background, de forma completamente assíncrona.
 * 
 * Benefícios:
 * - Não bloqueia a thread HTTP do controller
 * - Permite escalabilidade horizontal (múltiplas instâncias)
 * - Facilita implementação de retry em caso de falha
 * - Permite adicionar notificações quando o PDF estiver pronto
 * 
 * Fluxo atual:
 * 1. Recebe evento de solicitação de relatório
 * 2. Busca dados necessários (lançamentos, dashboard, etc.)
 * 3. Gera PDF usando PdfService
 * 4. Salva PDF em disco (futuramente: envia notificação ao usuário)
 * 
 * Futuras melhorias:
 * - Salvar PDF em storage (S3, Azure Blob, etc.)
 * - Enviar e-mail com link de download
 * - Criar registro na tabela de relatórios gerados
 * - Implementar retry automático em caso de falha
 * - Adicionar métricas de tempo de geração
 * 
 * @author SCF-MEI Team
 * @since 1.0.0
 */
@Component
public class ReportGenerationListener {

    private static final Logger logger = LoggerFactory.getLogger(ReportGenerationListener.class);

    @Autowired
    private PdfService pdfService;

    @Autowired
    private LancamentoService lancamentoService;

    @Autowired
    private DashboardService dashboardService;

    /**
     * Manipula o evento de solicitação de geração de relatório.
     * 
     * Este método é executado de forma assíncrona (@Async) em uma thread separada,
     * não bloqueando a resposta HTTP ao usuário.
     * 
     * @param event O evento contendo todos os parâmetros necessários para gerar o relatório
     */
    @EventListener
    @Async
    public void handleReportGenerationRequest(ReportGenerationRequestedEvent event) {
        String tipoRelatorio = event.getTipoRelatorio();
        Usuario usuario = event.getUsuario();
        
        logger.info("========================================");
        logger.info("INICIANDO GERAÇÃO DE RELATÓRIO EM BACKGROUND");
        logger.info("Tipo: {}", tipoRelatorio);
        logger.info("Usuário: {}", usuario.getUsername());
        logger.info("Data/Hora: {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        logger.info("========================================");

        try {
            byte[] pdfBytes = null;
            String nomeArquivo = null;

            switch (tipoRelatorio) {
                case "FATURAMENTO_DINAMICO":
                    pdfBytes = gerarRelatorioFaturamentoDinamico(event);
                    nomeArquivo = "relatorio_faturamento_" + event.getTipoVisao().toLowerCase() + "_" + System.currentTimeMillis() + ".pdf";
                    break;

                case "COMPRAS_COM_NOTA":
                    pdfBytes = gerarRelatorioComprasComNota(event);
                    nomeArquivo = "relatorio_compras_com_nota_" + System.currentTimeMillis() + ".pdf";
                    break;

                case "LANCAMENTOS":
                    pdfBytes = gerarRelatorioLancamentos(event);
                    nomeArquivo = "relatorio_lancamentos_" + System.currentTimeMillis() + ".pdf";
                    break;

                default:
                    logger.error("Tipo de relatório desconhecido: {}", tipoRelatorio);
                    return;
            }

            if (pdfBytes != null) {
                // TODO: Salvar PDF em storage (S3, Azure Blob, etc.)
                // TODO: Criar registro na tabela de relatórios gerados
                // TODO: Enviar notificação ao usuário (e-mail, push, etc.)
                
                logger.info("========================================");
                logger.info("RELATÓRIO GERADO COM SUCESSO!");
                logger.info("Tipo: {}", tipoRelatorio);
                logger.info("Usuário: {}", usuario.getUsername());
                logger.info("Nome do arquivo: {}", nomeArquivo);
                logger.info("Tamanho: {} KB", pdfBytes.length / 1024);
                logger.info("========================================");
                
                // Por enquanto, apenas logamos o sucesso
                // Em produção, você salvaria o PDF e notificaria o usuário
            }

        } catch (Exception e) {
            logger.error("========================================");
            logger.error("ERRO AO GERAR RELATÓRIO!");
            logger.error("Tipo: {}", tipoRelatorio);
            logger.error("Usuário: {}", usuario.getUsername());
            logger.error("Erro: {}", e.getMessage(), e);
            logger.error("========================================");
            
            // TODO: Implementar retry automático
            // TODO: Notificar usuário sobre o erro
            // TODO: Registrar falha em sistema de monitoramento
        }
    }

    /**
     * Gera relatório de faturamento dinâmico.
     */
    private byte[] gerarRelatorioFaturamentoDinamico(ReportGenerationRequestedEvent event) throws Exception {
        Usuario usuario = event.getUsuario();
        String tipoVisao = event.getTipoVisao();
        LocalDate dataInicio = event.getDataInicio();
        LocalDate dataFim = event.getDataFim();

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
                lancamentos = lancamentoService.buscarComFiltros(dataInicio, dataFim, event.getContaId(), 
                    event.getContatoId(), TipoLancamento.SAIDA, event.getCategoriaId(), true, 
                    event.getDescricao(), event.getStatus(), usuario);
                variaveis.put("faturamentoAnual", faturamentoAnual);
            } else {
                tituloVisao = "OFICIAL".equals(tipoVisao) ? "Faturamento Oficial" : "Faturamento Bancário";
                BigDecimal faturamentoAnual = dashboardService.getFaturamentoOficial(anoAtual, usuario);
                lancamentos = lancamentoService.buscarComFiltros(dataInicio, dataFim, event.getContaId(), 
                    event.getContatoId(), TipoLancamento.ENTRADA, event.getCategoriaId(), 
                    event.getComNotaFiscal(), event.getDescricao(), event.getStatus(), usuario);
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
            lancamentos = lancamentoService.buscarComFiltros(dataInicio, dataFim, event.getContaId(), 
                event.getContatoId(), event.getTipo(), event.getCategoriaId(), event.getComNotaFiscal(), 
                event.getDescricao(), event.getStatus(), usuario);
            BigDecimal total = lancamentos.stream().map(Lancamento::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
            variaveis.put("total", total);
        }

        variaveis.put("lancamentos", lancamentos);
        variaveis.put("dataInicio", dataInicio);
        variaveis.put("dataFim", dataFim);

        CompletableFuture<byte[]> pdfFuture = pdfService.gerarPdfDeHtml(templateNome, variaveis);
        return pdfFuture.get();
    }

    /**
     * Gera relatório de compras com nota fiscal.
     */
    private byte[] gerarRelatorioComprasComNota(ReportGenerationRequestedEvent event) throws Exception {
        Usuario usuario = event.getUsuario();
        
        List<Lancamento> lancamentos = lancamentoService.buscarComFiltros(
                event.getDataInicio(), event.getDataFim(), event.getContaId(), event.getContatoId(), 
                TipoLancamento.SAIDA, event.getCategoriaId(), true, event.getDescricao(), 
                event.getStatus(), usuario);

        BigDecimal total = lancamentos.stream()
                .map(Lancamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> variaveis = new HashMap<>();
        variaveis.put("lancamentos", lancamentos);
        variaveis.put("total", total);
        variaveis.put("dataInicio", event.getDataInicio());
        variaveis.put("dataFim", event.getDataFim());

        CompletableFuture<byte[]> pdfFuture = pdfService.gerarPdfDeHtml("relatorio_compras_com_nota", variaveis);
        return pdfFuture.get();
    }

    /**
     * Gera relatório de lançamentos.
     */
    private byte[] gerarRelatorioLancamentos(ReportGenerationRequestedEvent event) throws Exception {
        Usuario usuario = event.getUsuario();
        
        List<Lancamento> lancamentos = lancamentoService.buscarComFiltros(
                event.getDataInicio(), event.getDataFim(), event.getContaId(), event.getContatoId(), 
                event.getTipo(), event.getCategoriaId(), event.getComNotaFiscal(), 
                event.getDescricao(), event.getStatus(), usuario);

        // Process file paths to be accessible in the PDF
        for (Lancamento lancamento : lancamentos) {
            for (Comprovante comprovante : lancamento.getComprovantes()) {
                String currentPath = comprovante.getPathArquivo();
                if (currentPath != null && !currentPath.startsWith("/uploads/")) {
                    comprovante.setPathArquivo("/uploads/" + currentPath);
                }
            }
        }

        Map<String, Object> variaveis = new HashMap<>();
        variaveis.put("lancamentos", lancamentos);
        variaveis.put("dataInicio", event.getDataInicio());
        variaveis.put("dataFim", event.getDataFim());

        CompletableFuture<byte[]> pdfFuture = pdfService.gerarPdfDeHtml("relatorio_lancamentos", variaveis);
        return pdfFuture.get();
    }
}

