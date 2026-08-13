package com.example.ngoun.controller;

import com.example.ngoun.dto.BookingMediaRequest;
import com.example.ngoun.dto.BookingPropertyRequest;
import com.example.ngoun.model.BookingMedia;
import com.example.ngoun.model.BookingProperty;
import com.example.ngoun.service.BookingPropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BookingPropertyController {

    private final BookingPropertyService service;

    // --- Properties ---

    @GetMapping("/api/booking-properties")
    public List<BookingProperty> getPublished() {
        return service.findPublished();
    }

    @GetMapping("/api/booking-properties/admin")
    public List<BookingProperty> getAll() {
        return service.findAll();
    }

    @GetMapping("/api/booking-properties/{id}")
    public ResponseEntity<BookingProperty> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/api/booking-properties")
    public BookingProperty create(@RequestBody BookingPropertyRequest req) {
        return service.create(req);
    }

    @PutMapping("/api/booking-properties/{id}")
    public ResponseEntity<BookingProperty> update(@PathVariable Long id, @RequestBody BookingPropertyRequest req) {
        return service.update(id, req)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/booking-properties/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // --- Media sub-resource ---

    @GetMapping("/api/booking-properties/{id}/media")
    public List<BookingMedia> getMedia(@PathVariable Long id) {
        return service.getMedia(id);
    }

    @PostMapping("/api/booking-properties/{id}/media")
    public ResponseEntity<BookingMedia> addMedia(@PathVariable Long id, @RequestBody BookingMediaRequest req) {
        return service.addMedia(id, req)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/booking-media/{mediaId}")
    public ResponseEntity<Void> deleteMedia(@PathVariable Long mediaId) {
        service.deleteMedia(mediaId);
        return ResponseEntity.noContent().build();
    }
}
