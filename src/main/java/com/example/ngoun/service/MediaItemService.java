package com.example.ngoun.service;

import com.example.ngoun.model.MediaItem;
import com.example.ngoun.repository.MediaItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MediaItemService {
    private final MediaItemRepository repository;

    public List<MediaItem> findAll() {
        return repository.findAll();
    }

    public List<MediaItem> findByType(String type) {
        return repository.findByType(type);
    }

    public Optional<MediaItem> findById(Long id) {
        return repository.findById(id);
    }

    public MediaItem create(MediaItem mediaItem) {
        mediaItem.setCreatedAt(LocalDateTime.now());
        return repository.save(mediaItem);
    }

    public MediaItem update(Long id, MediaItem mediaItem) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setType(mediaItem.getType());
                    existing.setUrl(mediaItem.getUrl());
                    existing.setTitle(mediaItem.getTitle());
                    existing.setDescription(mediaItem.getDescription());
                    existing.setPublished(mediaItem.getPublished());
                    return repository.save(existing);
                }).orElse(null);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
