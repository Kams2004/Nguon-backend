package com.example.ngoun.service;

import com.example.ngoun.service.FileCompressionService.CompressionProfile;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MinioService {
    private final MinioClient minioClient;
    private final FileCompressionService compressionService;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioInternalUrl;

    @Value("${minio.external-url}")
    private String minioExternalUrl;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public void createBucketIfNotExists() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
            
            String policy = "{"
                    + "\"Version\":\"2012-10-17\","
                    + "\"Statement\":[{"
                    + "\"Effect\":\"Allow\","
                    + "\"Principal\":{\"AWS\":[\"*\"]},"
                    + "\"Action\":[\"s3:GetObject\"],"
                    + "\"Resource\":[\"arn:aws:s3:::" + bucketName + "/*\"]"
                    + "}]}";            
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(bucketName)
                    .config(policy)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Error creating bucket: " + e.getMessage());
        }
    }

    public String uploadFile(MultipartFile file, String folder) {
        return uploadFile(file, folder, CompressionProfile.AFFICHE);
    }

    public String uploadFile(MultipartFile file, String folder, CompressionProfile profile) {
        try {
            createBucketIfNotExists();
            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
            // Normaliser l'extension selon le type compressé
            FileCompressionService.CompressedFile compressed = compressionService.compress(
                    file.getInputStream(), file.getContentType(), originalName, profile);

            String extension = compressed.contentType().equals("image/jpeg") ? ".jpg"
                    : compressed.contentType().equals("application/pdf") ? ".pdf"
                    : originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.')) : "";

            String fileName = folder + "/" + System.currentTimeMillis() + "_"
                    + originalName.replaceAll("\\.[^.]+$", "") + extension;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(new ByteArrayInputStream(compressed.data()), compressed.data().length, -1)
                            .contentType(compressed.contentType())
                            .build()
            );
            return fileName;
        } catch (Exception e) {
            throw new RuntimeException("Error uploading file: " + e.getMessage());
        }
    }

    public String getPresignedUrl(String objectName, int expiryMinutes) {
        try {
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expiryMinutes, TimeUnit.MINUTES)
                            .build()
            );
            return url.replace(minioInternalUrl, minioExternalUrl);
        } catch (Exception e) {
            throw new RuntimeException("Error generating presigned URL: " + e.getMessage());
        }
    }

    public String getDownloadUrl(String objectName, int expiryMinutes) {
        try {
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expiryMinutes, TimeUnit.MINUTES)
                            .build()
            );
            return url.replace(minioInternalUrl, minioExternalUrl) + "&response-content-disposition=attachment";
        } catch (Exception e) {
            throw new RuntimeException("Error generating download URL: " + e.getMessage());
        }
    }

    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error deleting file: " + e.getMessage());
        }
    }

    public InputStream getFileStream(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error getting file: " + e.getMessage());
        }
    }
}
