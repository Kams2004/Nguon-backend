package com.example.ngoun.controller;

import com.example.ngoun.dto.PageResponse;
import com.example.ngoun.model.MediaItem;
import com.example.ngoun.service.MediaItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mediatheque")
@RequiredArgsConstructor
public class MediaItemController {
    private final MediaItemService service;

    @GetMapping
    public List<MediaItem> getAll(@RequestParam(required = false) String type) {
        return type != null ? service.findByType(type) : service.findAll();
    }

    @GetMapping("/paged")
    public PageResponse<MediaItem> getPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        return PageResponse.of(service.findPaged(page, size, search));
    }

    @GetMapping("/{id}")
    public MediaItem getById(@PathVariable Long id) {
        return service.findById(id).orElse(null);
    }

    @PostMapping
    public MediaItem create(@RequestBody MediaItem mediaItem) {
        return service.create(mediaItem);
    }

    @PutMapping("/{id}")
    public MediaItem update(@PathVariable Long id, @RequestBody MediaItem mediaItem) {
        return service.update(id, mediaItem);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
