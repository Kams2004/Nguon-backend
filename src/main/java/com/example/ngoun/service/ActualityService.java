package com.example.ngoun.service;

import com.example.ngoun.model.Actuality;
import com.example.ngoun.repository.ActualityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActualityService {
    private final ActualityRepository actualityRepository;
    
    public List<Actuality> getAllActualities() {
        return actualityRepository.findAll();
    }
    
    public List<Actuality> getPublishedActualities() {
        return actualityRepository.findByPublishedTrue();
    }
    
    public Actuality getActualityById(Long id) {
        return actualityRepository.findById(id).orElse(null);
    }
    
    public Actuality createActuality(Actuality actuality) {
        actuality.setCreatedAt(LocalDateTime.now());
        return actualityRepository.save(actuality);
    }
    
    public Actuality updateActuality(Long id, Actuality actuality) {
        Actuality existing = actualityRepository.findById(id).orElse(null);
        if (existing != null) {
            existing.setTitle(actuality.getTitle());
            existing.setDescription(actuality.getDescription());
            existing.setMedia(actuality.getMedia());
            existing.setPublished(actuality.getPublished());
            return actualityRepository.save(existing);
        }
        return null;
    }
    
    public void deleteActuality(Long id) {
        actualityRepository.deleteById(id);
    }
}
