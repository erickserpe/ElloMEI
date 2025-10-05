package br.com.ellomei.service;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    public List<String> storeFile(MultipartFile file) {
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        try {
            if (originalFileName.contains("..")) {
                throw new RuntimeException("Nome de arquivo inválido: " + originalFileName);
            }
            if ("application/pdf".equals(file.getContentType())) {
                return storePdfAsImages(file, originalFileName);
            } else {
                return Collections.singletonList(storeImageDirectly(file, originalFileName));
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

    private List<String> storePdfAsImages(MultipartFile file, String originalFileName) throws IOException {
        List<String> generatedFiles = new ArrayList<>();
        String fileNameWithoutExt = originalFileName.replaceFirst("[.][^.]+$", "");
        String fileIdentifier = UUID.randomUUID().toString();

        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                String newImageName = fileIdentifier + "_" + fileNameWithoutExt + "_page_" + (page + 1) + ".png";
                Path targetLocation = this.fileStorageLocation.resolve(newImageName);
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 150);
                ImageIO.write(bim, "PNG", targetLocation.toFile());
                generatedFiles.add(newImageName);
            }
        }
        return generatedFiles;
    }

    public Path getFileStorageLocation() {
        return this.fileStorageLocation;
    }
}