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

    public List<Programme> findAll() {
        return repository.findAll();
    }

    public List<Programme> findByDate(LocalDate date) {
        return repository.findByDate(date);
    }

    public Optional<Programme> findById(Long id) {
        return repository.findById(id);
    }

    public Programme create(Programme programme) {
        if (repository.findByDayOrder(programme.getDayOrder()).isPresent()) {
            throw new IllegalArgumentException("Programme with day order " + programme.getDayOrder() + " already exists");
        }
        return repository.save(programme);
    }

    public Programme update(Long id, Programme programme) {
        return repository.findById(id)
                .map(existing -> {
                    if (!existing.getDayOrder().equals(programme.getDayOrder()) && 
                        repository.findByDayOrder(programme.getDayOrder()).isPresent()) {
                        throw new IllegalArgumentException("Programme with day order " + programme.getDayOrder() + " already exists");
                    }
                    existing.setDayOrder(programme.getDayOrder());
                    existing.setDate(programme.getDate());
                    existing.setStartTime(programme.getStartTime());
                    existing.setEndTime(programme.getEndTime());
                    existing.setLocation(programme.getLocation());
                    existing.setActivity(programme.getActivity());
                    existing.setImageUrl(programme.getImageUrl());
                    existing.setPdfUrl(programme.getPdfUrl());
                    existing.setPublished(programme.getPublished());
                    return repository.save(existing);
                }).orElse(null);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
