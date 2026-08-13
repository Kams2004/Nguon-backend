package com.example.ngoun.service;

import com.example.ngoun.model.ManifestationSite;
import com.example.ngoun.repository.ManifestationSiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ManifestationSiteService {
    private final ManifestationSiteRepository repository;
    private final PresignedUrlCache urlCache;

    public List<ManifestationSite> findAll() {
        return repository.findAll().stream().map(this::enrich).toList();
    }

    public List<ManifestationSite> findPublished() {
        return repository.findByPublishedTrue().stream().map(this::enrich).toList();
    }

    public Optional<ManifestationSite> findById(Long id) {
        return repository.findById(id).map(this::enrich);
    }

    public ManifestationSite create(ManifestationSite site) {
        if (repository.findByTownTitle(site.getTownTitle()).isPresent()) {
            throw new IllegalArgumentException("Site with town title '" + site.getTownTitle() + "' already exists");
        }
        return enrich(repository.save(site));
    }

    public ManifestationSite update(Long id, ManifestationSite site) {
        return repository.findById(id).map(existing -> {
            if (!existing.getTownTitle().equals(site.getTownTitle()) &&
                repository.findByTownTitle(site.getTownTitle()).isPresent()) {
                throw new IllegalArgumentException("Site with town title '" + site.getTownTitle() + "' already exists");
            }
            urlCache.invalidate(existing.getImage());
            existing.setImage(site.getImage());
            existing.setTownTitle(site.getTownTitle());
            existing.setSubTownTitles(site.getSubTownTitles());
            existing.setPublished(site.getPublished());
            return enrich(repository.save(existing));
        }).orElse(null);
    }

    public void delete(Long id) {
        repository.findById(id).ifPresent(s -> {
            urlCache.invalidate(s.getImage());
            repository.delete(s);
        });
    }

    private ManifestationSite enrich(ManifestationSite s) {
        s.setPresignedUrl(urlCache.get(s.getImage()));
        return s;
    }
}
