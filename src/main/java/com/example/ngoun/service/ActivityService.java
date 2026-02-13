package com.example.ngoun.service;

import com.example.ngoun.model.Activity;
import com.example.ngoun.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ActivityService {
    private final ActivityRepository repository;

    public List<Activity> findAll() {
        return repository.findAll();
    }

    public Optional<Activity> findById(Long id) {
        return repository.findById(id);
    }

    public Activity create(Activity activity) {
        activity.setCreatedAt(LocalDateTime.now());
        return repository.save(activity);
    }

    public Activity update(Long id, Activity activity) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setName(activity.getName());
                    existing.setDescription(activity.getDescription());
                    existing.setPublished(activity.getPublished());
                    return repository.save(existing);
                }).orElse(null);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
