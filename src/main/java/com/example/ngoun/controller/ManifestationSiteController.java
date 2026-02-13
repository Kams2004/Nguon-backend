package com.example.ngoun.controller;

import com.example.ngoun.model.ManifestationSite;
import com.example.ngoun.service.ManifestationSiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manifestation-sites")
@RequiredArgsConstructor
public class ManifestationSiteController {
    private final ManifestationSiteService service;

    @GetMapping
    public List<ManifestationSite> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ManifestationSite getById(@PathVariable Long id) {
        return service.findById(id).orElse(null);
    }

    @PostMapping
    public ManifestationSite create(@RequestBody ManifestationSite site) {
        return service.create(site);
    }

    @PutMapping("/{id}")
    public ManifestationSite update(@PathVariable Long id, @RequestBody ManifestationSite site) {
        return service.update(id, site);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}
