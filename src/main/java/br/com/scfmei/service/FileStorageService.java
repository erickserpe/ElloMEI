package br.com.scfmei.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Não foi possível criar o diretório para armazenar os arquivos.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        try {
            if (originalFileName.contains("..")) {
                throw new RuntimeException("Nome de arquivo inválido: " + originalFileName);
            }

            // Verifica se o arquivo é um PDF
            if ("application/pdf".equals(file.getContentType())) {
                return storePdfAsImage(file, originalFileName);
            } else {
                // Se for imagem ou outro tipo, salva diretamente
                return storeImageDirectly(file, originalFileName);
            }

        } catch (IOException ex) {
            throw new RuntimeException("Não foi possível armazenar o arquivo " + originalFileName, ex);
        }
    }

    private String storeImageDirectly(MultipartFile file, String originalFileName) throws IOException {
        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;
        Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        return uniqueFileName;
    }

    private String storePdfAsImage(MultipartFile file, String originalFileName) throws IOException {
        // Remove a extensão .pdf e prepara o nome para .png
        String fileNameWithoutExt = originalFileName.replaceFirst("[.][^.]+$", "");
        String newImageName = UUID.randomUUID().toString() + "_" + fileNameWithoutExt + ".png";
        Path targetLocation = this.fileStorageLocation.resolve(newImageName);

        // Usa a biblioteca PDFBox para carregar o PDF
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            // Renderiza a primeira página (página 0) como uma imagem
            BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 150); // 150 DPI é uma boa qualidade

            // Salva a imagem renderizada como um arquivo PNG
            ImageIO.write(bim, "PNG", targetLocation.toFile());
        }

        return newImageName;
    }
    public Path getFileStorageLocation() {
        return this.fileStorageLocation;
    }
}