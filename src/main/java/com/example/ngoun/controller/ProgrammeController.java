package com.example.ngoun.controller;

import com.example.ngoun.model.Programme;
import com.example.ngoun.service.ProgrammeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/programmes")
@RequiredArgsConstructor
public class ProgrammeController {
    private final ProgrammeService service;

    @GetMapping
    public List<Programme> getAll(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return date != null ? service.findByDate(date) : service.findAll();
    }

    @GetMapping("/{id}")
    public Programme getById(@PathVariable Long id) {
        return service.findById(id).orElse(null);
    }

    @PostMapping
    public Programme create(@RequestBody Programme programme) {
        return service.create(programme);
    }

    @PutMapping("/{id}")
    public Programme update(@PathVariable Long id, @RequestBody Programme programme) {
        return service.update(id, programme);
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
