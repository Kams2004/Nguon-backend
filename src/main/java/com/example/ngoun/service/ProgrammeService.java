package com.example.ngoun.service;

import com.example.ngoun.model.Programme;
import com.example.ngoun.repository.ProgrammeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProgrammeService {
    private final ProgrammeRepository repository;
    private final PresignedUrlCache urlCache;

    public List<Programme> findAll() {
        return repository.findAll().stream().map(this::enrich).toList();
    }

    public List<Programme> findByDate(LocalDate date) {
        return repository.findByDate(date).stream().map(this::enrich).toList();
    }

    public Optional<Programme> findById(Long id) {
        return repository.findById(id).map(this::enrich);
    }

    public Programme create(Programme programme) {
        if (repository.findByDayOrder(programme.getDayOrder()).isPresent()) {
            throw new IllegalArgumentException("Programme with day order " + programme.getDayOrder() + " already exists");
        }
        return enrich(repository.save(programme));
    }

    public Programme update(Long id, Programme programme) {
        return repository.findById(id).map(existing -> {
            if (!existing.getDayOrder().equals(programme.getDayOrder()) &&
                repository.findByDayOrder(programme.getDayOrder()).isPresent()) {
                throw new IllegalArgumentException("Programme with day order " + programme.getDayOrder() + " already exists");
            }
            urlCache.invalidate(existing.getImageUrl());
            urlCache.invalidate(existing.getPdfUrl());
            existing.setDayOrder(programme.getDayOrder());
            existing.setDate(programme.getDate());
            existing.setStartTime(programme.getStartTime());
            existing.setEndTime(programme.getEndTime());
            existing.setLocation(programme.getLocation());
            existing.setActivity(programme.getActivity());
            existing.setImageUrl(programme.getImageUrl());
            existing.setPdfUrl(programme.getPdfUrl());
            existing.setPublished(programme.getPublished());
            return enrich(repository.save(existing));
        }).orElse(null);
    }

    public void delete(Long id) {
        repository.findById(id).ifPresent(p -> {
            urlCache.invalidate(p.getImageUrl());
            urlCache.invalidate(p.getPdfUrl());
            repository.delete(p);
        });
    }

    private Programme enrich(Programme p) {
        p.setImagePresignedUrl(urlCache.get(p.getImageUrl()));
        p.setPdfPresignedUrl(urlCache.get(p.getPdfUrl()));
        return p;
    }
}
