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
        if (repository.findByName(activity.getName()).isPresent()) {
            throw new IllegalArgumentException("Activity with name '" + activity.getName() + "' already exists");
        }
        if (repository.findByDisplayOrder(activity.getDisplayOrder()).isPresent()) {
            throw new IllegalArgumentException("Activity with order " + activity.getDisplayOrder() + " already exists");
        }
        activity.setCreatedAt(LocalDateTime.now());
        return repository.save(activity);
    }

    public Activity update(Long id, Activity activity) {
        return repository.findById(id)
                .map(existing -> {
                    if (!existing.getName().equals(activity.getName()) && 
                        repository.findByName(activity.getName()).isPresent()) {
                        throw new IllegalArgumentException("Activity with name '" + activity.getName() + "' already exists");
                    }
                    if (!existing.getDisplayOrder().equals(activity.getDisplayOrder()) && 
                        repository.findByDisplayOrder(activity.getDisplayOrder()).isPresent()) {
                        throw new IllegalArgumentException("Activity with order " + activity.getDisplayOrder() + " already exists");
                    }
                    existing.setName(activity.getName());
                    existing.setDescription(activity.getDescription());
                    existing.setImage(activity.getImage());
                    existing.setDisplayOrder(activity.getDisplayOrder());
                    existing.setPublished(activity.getPublished());
                    return repository.save(existing);
                }).orElse(null);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
