package br.com.scfmei.service;

import com.lowagie.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
public class PdfService {

    @Autowired
    private TemplateEngine templateEngine;

    // Injetamos o FileStorageService para saber onde estão os arquivos
    @Autowired
    private FileStorageService fileStorageService;

    public byte[] gerarPdfDeHtml(String templateNome, Map<String, Object> variaveis) {
        Context context = new Context();
        context.setVariables(variaveis);

        String html = templateEngine.process(templateNome, context);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ITextRenderer renderer = new ITextRenderer();

            // --- MUDANÇA PRINCIPAL AQUI ---
            // Dizemos ao renderizador de PDF onde encontrar os arquivos (imagens, etc.)
            // Ele usará a nossa pasta 'uploads' como o endereço base.
            String baseUrl = fileStorageService.getFileStorageLocation().toUri().toURL().toString();
            renderer.setDocumentFromString(html, baseUrl);
            // --- FIM DA MUDANÇA ---

            renderer.layout();
            renderer.createPDF(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF: " + e.getMessage(), e);
        }
    }
}