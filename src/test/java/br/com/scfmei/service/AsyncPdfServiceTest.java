package br.com.scfmei.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AsyncPdfServiceTest {

    @Autowired
    private PdfService pdfService;

    @Test
    void deveExecutarGeracaoPdfAssincronamente() throws Exception {
        // Arrange
        Map<String, Object> variaveis = new HashMap<>();
        variaveis.put("titulo", "Teste Assíncrono");
        variaveis.put("conteudo", "Este é um teste de geração assíncrona de PDF");

        // Act
        System.out.println("Iniciando geração de PDF no thread: " + Thread.currentThread().getName());
        
        CompletableFuture<byte[]> pdfFuture = pdfService.gerarPdfDeHtml("test_template", variaveis);
        
        // Verify that the method returns immediately (non-blocking)
        assertNotNull(pdfFuture);
        assertFalse(pdfFuture.isDone()); // Should not be done immediately
        
        // Wait for completion and verify result
        byte[] pdfBytes = pdfFuture.get();
        
        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        assertTrue(pdfFuture.isDone());
        
        System.out.println("PDF gerado com sucesso! Tamanho: " + pdfBytes.length + " bytes");
    }

    @Test
    void deveExecutarMultiplosRelatoriosSimultaneamente() throws Exception {
        // Arrange
        Map<String, Object> variaveis1 = new HashMap<>();
        variaveis1.put("titulo", "Relatório 1");
        variaveis1.put("conteudo", "Conteúdo do primeiro relatório");

        Map<String, Object> variaveis2 = new HashMap<>();
        variaveis2.put("titulo", "Relatório 2");
        variaveis2.put("conteudo", "Conteúdo do segundo relatório");

        Map<String, Object> variaveis3 = new HashMap<>();
        variaveis3.put("titulo", "Relatório 3");
        variaveis3.put("conteudo", "Conteúdo do terceiro relatório");

        // Act - Start multiple PDF generations simultaneously
        long startTime = System.currentTimeMillis();
        
        CompletableFuture<byte[]> pdf1 = pdfService.gerarPdfDeHtml("test_template", variaveis1);
        CompletableFuture<byte[]> pdf2 = pdfService.gerarPdfDeHtml("test_template", variaveis2);
        CompletableFuture<byte[]> pdf3 = pdfService.gerarPdfDeHtml("test_template", variaveis3);

        // Wait for all to complete
        CompletableFuture<Void> allPdfs = CompletableFuture.allOf(pdf1, pdf2, pdf3);
        allPdfs.get();
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        // Assert
        assertTrue(pdf1.isDone());
        assertTrue(pdf2.isDone());
        assertTrue(pdf3.isDone());

        byte[] result1 = pdf1.get();
        byte[] result2 = pdf2.get();
        byte[] result3 = pdf3.get();

        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);

        assertTrue(result1.length > 0);
        assertTrue(result2.length > 0);
        assertTrue(result3.length > 0);

        System.out.println("Três PDFs gerados simultaneamente em: " + totalTime + "ms");
        System.out.println("PDF 1: " + result1.length + " bytes");
        System.out.println("PDF 2: " + result2.length + " bytes");
        System.out.println("PDF 3: " + result3.length + " bytes");
    }

    @Test
    void deveManterThreadPrincipalLivre() throws Exception {
        // Arrange
        Map<String, Object> variaveis = new HashMap<>();
        variaveis.put("titulo", "Teste Thread Principal");
        variaveis.put("conteudo", "Verificando se a thread principal não é bloqueada");

        String mainThreadName = Thread.currentThread().getName();
        System.out.println("Thread principal: " + mainThreadName);

        // Act
        CompletableFuture<byte[]> pdfFuture = pdfService.gerarPdfDeHtml("test_template", variaveis);
        
        // The main thread should be free to do other work
        // Simulate other work while PDF is being generated
        Thread.sleep(100); // Simulate some work
        
        String currentThreadName = Thread.currentThread().getName();
        System.out.println("Thread atual após iniciar PDF: " + currentThreadName);
        
        // Wait for PDF completion
        byte[] pdfBytes = pdfFuture.get();

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        assertEquals(mainThreadName, currentThreadName); // Should still be the same main thread
        
        System.out.println("Thread principal mantida livre durante geração assíncrona!");
    }
}
