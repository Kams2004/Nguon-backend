package com.example.ngoun.controller;

import com.example.ngoun.model.Activity;
import com.example.ngoun.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {
    private final ActivityService service;

    @GetMapping
    public List<Activity> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Activity getById(@PathVariable Long id) {
        return service.findById(id).orElse(null);
    }

    @PostMapping
    public Activity create(@RequestBody Activity activity) {
        return service.create(activity);
    }

    @PutMapping("/{id}")
    public Activity update(@PathVariable Long id, @RequestBody Activity activity) {
        return service.update(id, activity);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
