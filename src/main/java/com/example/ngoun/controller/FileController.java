package com.example.ngoun.controller;

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
        String fileName = minioService.uploadFile(file, "programmes");
        String presignedUrl = minioService.getPresignedUrl(fileName, 60);
        
        Map<String, String> response = new HashMap<>();
        response.put("fileName", fileName);
        response.put("presignedUrl", presignedUrl);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload/media")
    public ResponseEntity<Map<String, String>> uploadMediaFile(@RequestParam("file") MultipartFile file) {
        String fileName = minioService.uploadFile(file, "media");
        String presignedUrl = minioService.getPresignedUrl(fileName, 60);
        
        Map<String, String> response = new HashMap<>();
        response.put("fileName", fileName);
        response.put("presignedUrl", presignedUrl);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload/activity")
    public ResponseEntity<Map<String, String>> uploadActivityFile(@RequestParam("file") MultipartFile file) {
        String fileName = minioService.uploadFile(file, "activity");
        String presignedUrl = minioService.getPresignedUrl(fileName, 60);
        
        Map<String, String> response = new HashMap<>();
        response.put("fileName", fileName);
        response.put("presignedUrl", presignedUrl);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload/site")
    public ResponseEntity<Map<String, String>> uploadSiteFile(@RequestParam("file") MultipartFile file) {
        String fileName = minioService.uploadFile(file, "site");
        String presignedUrl = minioService.getPresignedUrl(fileName, 60);
        
        Map<String, String> response = new HashMap<>();
        response.put("fileName", fileName);
        response.put("presignedUrl", presignedUrl);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload/sponsor")
    public ResponseEntity<Map<String, String>> uploadSponsorFile(@RequestParam("file") MultipartFile file) {
        String fileName = minioService.uploadFile(file, "sponsors");
        String presignedUrl = minioService.getPresignedUrl(fileName, 60);
        
        Map<String, String> response = new HashMap<>();
        response.put("fileName", fileName);
        response.put("presignedUrl", presignedUrl);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload/actuality")
    public ResponseEntity<Map<String, String>> uploadActualityFile(@RequestParam("file") MultipartFile file) {
        String fileName = minioService.uploadFile(file, "actualities");
        String presignedUrl = minioService.getPresignedUrl(fileName, 60);
        
        Map<String, String> response = new HashMap<>();
        response.put("fileName", fileName);
        response.put("presignedUrl", presignedUrl);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/presigned-url")
    public ResponseEntity<Map<String, String>> getPresignedUrl(
            @RequestParam String fileName,
            @RequestParam(defaultValue = "60") int expiryMinutes) {
        String presignedUrl = minioService.getPresignedUrl(fileName, expiryMinutes);
        
        Map<String, String> response = new HashMap<>();
        response.put("presignedUrl", presignedUrl);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/download-url")
    public ResponseEntity<Map<String, String>> getDownloadUrl(
            @RequestParam String fileName,
            @RequestParam(defaultValue = "60") int expiryMinutes) {
        String downloadUrl = minioService.getDownloadUrl(fileName, expiryMinutes);
        
        Map<String, String> response = new HashMap<>();
        response.put("downloadUrl", downloadUrl);
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
            
            String contentType = "application/octet-stream";
            if (fileName.endsWith(".png")) contentType = "image/png";
            else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) contentType = "image/jpeg";
            else if (fileName.endsWith(".gif")) contentType = "image/gif";
            else if (fileName.endsWith(".mp4")) contentType = "video/mp4";
            else if (fileName.endsWith(".webm")) contentType = "video/webm";
            else if (fileName.endsWith(".pdf")) contentType = "application/pdf";
            
            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
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
            
            String contentType = "application/octet-stream";
            if (path.endsWith(".png")) contentType = "image/png";
            else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) contentType = "image/jpeg";
            else if (path.endsWith(".gif")) contentType = "image/gif";
            else if (path.endsWith(".mp4")) contentType = "video/mp4";
            else if (path.endsWith(".webm")) contentType = "video/webm";
            else if (path.endsWith(".pdf")) contentType = "application/pdf";
            
            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .header("Cache-Control", "public, max-age=31536000")
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
