package com.example.ngoun.controller;

import com.example.ngoun.model.Actuality;
import com.example.ngoun.service.ActualityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/actualities")
@RequiredArgsConstructor
public class ActualityController {
    private final ActualityService actualityService;
    
    @GetMapping
    public ResponseEntity<List<Actuality>> getAllActualities() {
        return ResponseEntity.ok(actualityService.getAllActualities());
    }
    
    @GetMapping("/published")
    public ResponseEntity<List<Actuality>> getPublishedActualities() {
        return ResponseEntity.ok(actualityService.getPublishedActualities());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Actuality> getActualityById(@PathVariable Long id) {
        Actuality actuality = actualityService.getActualityById(id);
        return actuality != null ? ResponseEntity.ok(actuality) : ResponseEntity.notFound().build();
    }
    
    @PostMapping
    public ResponseEntity<Actuality> createActuality(@RequestBody Actuality actuality) {
        return ResponseEntity.ok(actualityService.createActuality(actuality));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Actuality> updateActuality(@PathVariable Long id, @RequestBody Actuality actuality) {
        Actuality updated = actualityService.updateActuality(id, actuality);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActuality(@PathVariable Long id) {
        actualityService.deleteActuality(id);
        return ResponseEntity.noContent().build();
    }
}
