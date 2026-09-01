package com.example.ngoun.controller;

import com.example.ngoun.service.FileCompressionService.CompressionProfile;
import com.example.ngoun.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    private final MinioService minioService;

    @PostMapping("/upload/programme")
    public ResponseEntity<Map<String, String>> uploadProgrammeFile(@RequestParam("file") MultipartFile file) {
        return buildResponse(minioService.uploadFile(file, "programmes", CompressionProfile.AFFICHE));
    }

    @PostMapping("/upload/media")
    public ResponseEntity<Map<String, String>> uploadMediaFile(@RequestParam("file") MultipartFile file) {
        return buildResponse(minioService.uploadFile(file, "media", CompressionProfile.AFFICHE));
    }

    @PostMapping("/upload/activity")
    public ResponseEntity<Map<String, String>> uploadActivityFile(@RequestParam("file") MultipartFile file) {
        return buildResponse(minioService.uploadFile(file, "activity", CompressionProfile.AFFICHE));
    }

    @PostMapping("/upload/site")
    public ResponseEntity<Map<String, String>> uploadSiteFile(@RequestParam("file") MultipartFile file) {
        return buildResponse(minioService.uploadFile(file, "site", CompressionProfile.AFFICHE));
    }

    @PostMapping("/upload/sponsor")
    public ResponseEntity<Map<String, String>> uploadSponsorFile(@RequestParam("file") MultipartFile file) {
        return buildResponse(minioService.uploadFile(file, "sponsors", CompressionProfile.AFFICHE));
    }

    @PostMapping("/upload/actuality")
    public ResponseEntity<Map<String, String>> uploadActualityFile(@RequestParam("file") MultipartFile file) {
        return buildResponse(minioService.uploadFile(file, "actualities", CompressionProfile.AFFICHE));
    }

    @PostMapping("/upload/booking")
    public ResponseEntity<Map<String, String>> uploadBookingFile(@RequestParam("file") MultipartFile file) {
        return buildResponse(minioService.uploadFile(file, "booking", CompressionProfile.AFFICHE));
    }

    @PostMapping("/upload/shop")
    public ResponseEntity<Map<String, String>> uploadShopFile(@RequestParam("file") MultipartFile file) {
        return buildResponse(minioService.uploadFile(file, "shop", CompressionProfile.AFFICHE));
    }

    @PostMapping("/upload/concours/affiche")
    public ResponseEntity<Map<String, String>> uploadConcoursAffiche(@RequestParam("file") MultipartFile file) {
        return buildResponse(minioService.uploadFile(file, "concours/affiches", CompressionProfile.AFFICHE));
    }

    @PostMapping("/upload/concours/fiche")
    public ResponseEntity<Map<String, String>> uploadConcoursFiche(@RequestParam("file") MultipartFile file) {
        return buildResponse(minioService.uploadFile(file, "concours/fiches", CompressionProfile.FICHE_PDF));
    }

    @PostMapping("/upload/candidat/document")
    public ResponseEntity<Map<String, String>> uploadCandidatDocument(@RequestParam("file") MultipartFile file) {
        return buildResponse(minioService.uploadFile(file, "candidats/documents", CompressionProfile.DOCUMENT));
    }

    @PostMapping("/upload/candidat/signature")
    public ResponseEntity<Map<String, String>> uploadCandidatSignature(@RequestParam("file") MultipartFile file) {
        return buildResponse(minioService.uploadFile(file, "candidats/signatures", CompressionProfile.SIGNATURE));
    }

    @PostMapping("/upload/vote-profile")
    public ResponseEntity<Map<String, String>> uploadVoteProfileFile(@RequestParam("file") MultipartFile file) {
        return buildResponse(minioService.uploadFile(file, "vote-profiles", CompressionProfile.AFFICHE));
    }

    @GetMapping("/presigned-url")
    public ResponseEntity<Map<String, String>> getPresignedUrl(
            @RequestParam String fileName,
            @RequestParam(defaultValue = "60") int expiryMinutes) {
        Map<String, String> response = new HashMap<>();
        response.put("presignedUrl", minioService.getPresignedUrl(fileName, expiryMinutes));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/download-url")
    public ResponseEntity<Map<String, String>> getDownloadUrl(
            @RequestParam String fileName,
            @RequestParam(defaultValue = "60") int expiryMinutes) {
        Map<String, String> response = new HashMap<>();
        response.put("downloadUrl", minioService.getDownloadUrl(fileName, expiryMinutes));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFile(@RequestParam String fileName) {
        minioService.deleteFile(fileName);
        return ResponseEntity.ok("File deleted successfully");
    }

    private static final Set<String> VIDEO_EXTENSIONS = Set.of(".mp4", ".webm", ".ogg");

    @GetMapping("/thumbnail")
    public ResponseEntity<byte[]> getThumbnail(@RequestParam String path) {
        String lower = path.toLowerCase();
        if (VIDEO_EXTENSIONS.stream().anyMatch(lower::endsWith)) {
            return ResponseEntity.notFound().build();
        }
        try {
            InputStream stream = minioService.getFileStream(path);
            BufferedImage original = ImageIO.read(stream);
            stream.close();
            if (original == null) return ResponseEntity.notFound().build();

            int maxW = 1200, maxH = 630;
            int origW = original.getWidth(), origH = original.getHeight();
            double scale = Math.min((double) maxW / origW, (double) maxH / origH);
            int newW = (int) (origW * scale);
            int newH = (int) (origH * scale);

            BufferedImage resized = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resized.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(original, 0, 0, newW, newH, null);
            g.dispose();

            var jpegWriter = ImageIO.getImageWritersByFormatName("jpeg").next();
            var jpegParams = jpegWriter.getDefaultWriteParam();
            jpegParams.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
            jpegParams.setCompressionQuality(0.75f);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            jpegWriter.setOutput(ImageIO.createImageOutputStream(out));
            jpegWriter.write(null, new javax.imageio.IIOImage(resized, null, null), jpegParams);
            jpegWriter.dispose();

            return ResponseEntity.ok()
                    .header("Content-Type", "image/jpeg")
                    .header("Cache-Control", "public, max-age=86400")
                    .body(out.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/view/{folder}/{fileName}")
    public ResponseEntity<byte[]> viewFile(@PathVariable String folder, @PathVariable String fileName) {
        try {
            String objectName = folder + "/" + fileName;
            InputStream stream = minioService.getFileStream(objectName);
            byte[] content = stream.readAllBytes();
            stream.close();
            return ResponseEntity.ok()
                    .header("Content-Type", resolveContentType(fileName))
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/view/**")
    public ResponseEntity<byte[]> viewFileByPath(@RequestParam String path) {
        try {
            InputStream stream = minioService.getFileStream(path);
            byte[] content = stream.readAllBytes();
            stream.close();
            return ResponseEntity.ok()
                    .header("Content-Type", resolveContentType(path))
                    .header("Cache-Control", "public, max-age=31536000")
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    private ResponseEntity<Map<String, String>> buildResponse(String fileName) {
        Map<String, String> response = new HashMap<>();
        response.put("fileName", fileName);
        response.put("presignedUrl", minioService.getPresignedUrl(fileName, 60));
        return ResponseEntity.ok(response);
    }

    private String resolveContentType(String name) {
        String n = name.toLowerCase();
        if (n.endsWith(".png")) return "image/png";
        if (n.endsWith(".jpg") || n.endsWith(".jpeg")) return "image/jpeg";
        if (n.endsWith(".gif")) return "image/gif";
        if (n.endsWith(".mp4")) return "video/mp4";
        if (n.endsWith(".webm")) return "video/webm";
        if (n.endsWith(".pdf")) return "application/pdf";
        return "application/octet-stream";
    }
}
