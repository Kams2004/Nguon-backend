package com.example.ngoun.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MinioService {
    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.external-url}")
    private String externalUrl;

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

    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/jpg", "image/png", "image/webp");

    public String uploadFile(MultipartFile file, String folder) {
        try {
            createBucketIfNotExists();
            String fileName = folder + "/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            InputStream inputStream;
            long size;
            String contentType = file.getContentType();

            if (contentType != null && IMAGE_TYPES.contains(contentType.toLowerCase())) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Thumbnails.of(file.getInputStream())
                        .size(1200, 630)
                        .outputFormat("jpeg")
                        .outputQuality(0.75)
                        .toOutputStream(out);
                byte[] compressed = out.toByteArray();
                inputStream = new ByteArrayInputStream(compressed);
                size = compressed.length;
                contentType = "image/jpeg";
            } else {
                inputStream = file.getInputStream();
                size = file.getSize();
            }

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
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
            return url.replace("http://nguon-minio:9000", externalUrl)
                      .replace("http://minio:9000", externalUrl);
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
            ) + "&response-content-disposition=attachment";
            return url.replace("http://nguon-minio:9000", externalUrl)
                      .replace("http://minio:9000", externalUrl);
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
