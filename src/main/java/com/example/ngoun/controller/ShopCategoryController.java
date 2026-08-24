package com.example.ngoun.controller;

import com.example.ngoun.dto.ShopCategoryRequest;
import com.example.ngoun.model.ShopCategory;
import com.example.ngoun.service.ShopCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ShopCategoryController {

    private final ShopCategoryService service;

    @GetMapping("/api/shop-categories")
    public List<ShopCategory> getAll() {
        return service.findAll();
    }

    @GetMapping("/api/shop-categories/{id}")
    public ResponseEntity<ShopCategory> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/api/shop-categories")
    public ShopCategory create(@RequestBody ShopCategoryRequest req) {
        return service.create(req);
    }

    @PutMapping("/api/shop-categories/{id}")
    public ResponseEntity<ShopCategory> update(@PathVariable Long id, @RequestBody ShopCategoryRequest req) {
        return service.update(id, req)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/shop-categories/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
