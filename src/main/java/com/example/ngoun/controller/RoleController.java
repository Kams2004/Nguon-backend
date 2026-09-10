package com.example.ngoun.controller;

import com.example.ngoun.dto.PageResponse;
import com.example.ngoun.model.Role;
import com.example.ngoun.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService service;

    @GetMapping
    public List<Role> getAll() {
        return service.findAll();
    }

    @GetMapping("/paged")
    public PageResponse<Role> getPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        return PageResponse.of(service.findPaged(page, size, search));
    }

    @GetMapping("/{id}")
    public Role getById(@PathVariable Long id) {
        return service.findById(id).orElse(null);
    }

    @PostMapping
    public Role create(@RequestBody Role role) {
        return service.create(role);
    }

    @PutMapping("/{id}")
    public Role update(@PathVariable Long id, @RequestBody Role role) {
        return service.update(id, role);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
