package com.example.ngoun.service;

import com.example.ngoun.model.Actuality;
import com.example.ngoun.repository.ActualityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActualityService {
    private final ActualityRepository actualityRepository;
    private final PresignedUrlCache urlCache;

    public List<Actuality> getAllActualities() {
        return actualityRepository.findAll().stream().map(this::enrich).toList();
    }

    public Page<Actuality> getPaged(int page, int size, String search) {
        PageRequest request = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        String q = search == null ? "" : search.trim();
        Page<Actuality> result = q.isEmpty()
                ? actualityRepository.findAll(request)
                : actualityRepository.findByTitleContainingIgnoreCase(q, request);
        result.forEach(this::enrich);
        return result;
    }

    public List<Actuality> getPublishedActualities() {
        return actualityRepository.findByPublishedTrue().stream().map(this::enrich).toList();
    }

    public Actuality getActualityById(Long id) {
        return actualityRepository.findById(id).map(this::enrich).orElse(null);
    }

    public Actuality createActuality(Actuality actuality) {
        actuality.setCreatedAt(LocalDateTime.now());
        return enrich(actualityRepository.save(actuality));
    }

    public Actuality updateActuality(Long id, Actuality actuality) {
        return actualityRepository.findById(id).map(existing -> {
            urlCache.invalidate(existing.getMedia());
            existing.setTitle(actuality.getTitle());
            existing.setDescription(actuality.getDescription());
            existing.setMedia(actuality.getMedia());
            existing.setPublished(actuality.getPublished());
            return enrich(actualityRepository.save(existing));
        }).orElse(null);
    }

    public void deleteActuality(Long id) {
        actualityRepository.findById(id).ifPresent(a -> {
            urlCache.invalidate(a.getMedia());
            actualityRepository.delete(a);
        });
    }

    private Actuality enrich(Actuality a) {
        a.setPresignedUrl(urlCache.get(a.getMedia()));
        return a;
    }
}
