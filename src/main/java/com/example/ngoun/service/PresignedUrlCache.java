package com.example.ngoun.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory cache for MinIO presigned URLs.
 * URLs are valid 60 min; we cache for 55 min to avoid serving expired URLs.
 * Call invalidate(path) whenever a file is deleted or replaced.
 */
@Service
@RequiredArgsConstructor
public class PresignedUrlCache {

    private static final int EXPIRY_MINUTES = 60;
    private static final long CACHE_TTL_MS  = 55 * 60 * 1000L;

    private final MinioService minioService;

    private record CachedUrl(String url, long expiresAt) {}

    private final Map<String, CachedUrl> cache = new ConcurrentHashMap<>();

    /** Returns a presigned URL for the given MinIO object path, using cache when possible. */
    public String get(String objectPath) {
        if (objectPath == null || objectPath.isBlank()) return null;
        CachedUrl cached = cache.get(objectPath);
        if (cached != null && Instant.now().toEpochMilli() < cached.expiresAt()) {
            return cached.url();
        }
        String url = minioService.getPresignedUrl(objectPath, EXPIRY_MINUTES);
        cache.put(objectPath, new CachedUrl(url, Instant.now().toEpochMilli() + CACHE_TTL_MS));
        return url;
    }

    /** Must be called when a file is deleted or overwritten. */
    public void invalidate(String objectPath) {
        if (objectPath != null) cache.remove(objectPath);
    }
}
