package com.example.ngoun.service;

import com.example.ngoun.model.MediaItem;
import com.example.ngoun.repository.MediaItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MediaItemService {
    private final MediaItemRepository repository;
    private final PresignedUrlCache urlCache;

    public List<MediaItem> findAll() {
        return repository.findAll().stream().map(this::enrich).toList();
    }

    public Page<MediaItem> findPaged(int page, int size, String search) {
        PageRequest request = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        String q = search == null ? "" : search.trim();
        Page<MediaItem> result = q.isEmpty()
                ? repository.findAll(request)
                : repository.findByTitleContainingIgnoreCase(q, request);
        result.forEach(this::enrich);
        return result;
    }

    public List<MediaItem> findByType(String type) {
        return repository.findByType(type).stream().map(this::enrich).toList();
    }

    public Optional<MediaItem> findById(Long id) {
        return repository.findById(id).map(this::enrich);
    }

    public MediaItem create(MediaItem mediaItem) {
        mediaItem.setCreatedAt(LocalDateTime.now());
        return enrich(repository.save(mediaItem));
    }

    public MediaItem update(Long id, MediaItem mediaItem) {
        return repository.findById(id).map(existing -> {
            urlCache.invalidate(existing.getUrl());
            existing.setType(mediaItem.getType());
            existing.setUrl(mediaItem.getUrl());
            existing.setTitle(mediaItem.getTitle());
            existing.setDescription(mediaItem.getDescription());
            existing.setPublished(mediaItem.getPublished());
            return enrich(repository.save(existing));
        }).orElse(null);
    }

    public void delete(Long id) {
        repository.findById(id).ifPresent(m -> {
            urlCache.invalidate(m.getUrl());
            repository.delete(m);
        });
    }

    private MediaItem enrich(MediaItem m) {
        m.setPresignedUrl(urlCache.get(m.getUrl()));
        return m;
    }
}
