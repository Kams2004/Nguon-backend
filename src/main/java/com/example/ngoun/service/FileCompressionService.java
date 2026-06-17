package com.example.ngoun.service;

import net.coobird.thumbnailator.Thumbnails;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Service
public class FileCompressionService {

    public enum CompressionProfile {
        /** Affiches concours : bonne qualité visuelle, max 1200px */
        AFFICHE(1200, 800, 0.80f),
        /** Fiches descriptives PDF : compression modérée */
        FICHE_PDF(1000, 1000, 0.70f),
        /** Documents candidat (CNI, CV) : lisibilité prioritaire */
        DOCUMENT(1000, 1400, 0.75f),
        /** Signature candidat : petite taille */
        SIGNATURE(600, 400, 0.70f);

        final int maxWidth;
        final int maxHeight;
        final float quality;

        CompressionProfile(int maxWidth, int maxHeight, float quality) {
            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;
            this.quality = quality;
        }
    }

    private static final java.util.Set<String> IMAGE_TYPES =
            java.util.Set.of("image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif");

    private static final java.util.Set<String> IMAGE_EXTENSIONS =
            java.util.Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");

    public record CompressedFile(byte[] data, String contentType) {}

    public CompressedFile compress(InputStream inputStream, String originalContentType,
                                   String originalFilename, CompressionProfile profile) {
        try {
            String ct = originalContentType != null ? originalContentType.toLowerCase() : "";
            String name = originalFilename != null ? originalFilename.toLowerCase() : "";

            boolean isImage = IMAGE_TYPES.contains(ct)
                    || IMAGE_EXTENSIONS.stream().anyMatch(name::endsWith);
            boolean isPdf = ct.equals("application/pdf") || name.endsWith(".pdf");

            if (isImage) {
                return compressImage(inputStream, profile);
            } else if (isPdf) {
                return compressPdf(inputStream, profile);
            } else {
                return new CompressedFile(inputStream.readAllBytes(), originalContentType);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur de compression : " + e.getMessage(), e);
        }
    }

    private CompressedFile compressImage(InputStream inputStream, CompressionProfile profile) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thumbnails.of(inputStream)
                .size(profile.maxWidth, profile.maxHeight)
                .outputFormat("jpeg")
                .outputQuality(profile.quality)
                .toOutputStream(out);
        return new CompressedFile(out.toByteArray(), "image/jpeg");
    }

    private CompressedFile compressPdf(InputStream inputStream, CompressionProfile profile) throws Exception {
        byte[] pdfBytes = inputStream.readAllBytes();
        try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
            for (PDPage page : doc.getPages()) {
                compressImagesInPage(page.getResources(), doc, profile);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return new CompressedFile(out.toByteArray(), "application/pdf");
        }
    }

    private void compressImagesInPage(PDResources resources, PDDocument doc,
                                       CompressionProfile profile) throws Exception {
        if (resources == null) return;
        for (COSName name : resources.getXObjectNames()) {
            PDXObject xObject = resources.getXObject(name);
            if (xObject instanceof PDImageXObject image) {
                BufferedImage buffered = image.getImage();
                // Redimensionner si trop grande
                if (buffered.getWidth() > profile.maxWidth || buffered.getHeight() > profile.maxHeight) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    Thumbnails.of(buffered)
                            .size(profile.maxWidth, profile.maxHeight)
                            .outputFormat("jpeg")
                            .outputQuality(profile.quality)
                            .toOutputStream(out);
                    BufferedImage resized = ImageIO.read(new ByteArrayInputStream(out.toByteArray()));
                    PDImageXObject compressed = JPEGFactory.createFromImage(doc, resized, profile.quality);
                    resources.put(name, compressed);
                }
            }
        }
    }
}
