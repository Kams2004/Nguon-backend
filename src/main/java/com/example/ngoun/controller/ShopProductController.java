package com.example.ngoun.controller;

import com.example.ngoun.dto.ShopProductRequest;
import com.example.ngoun.model.ShopProduct;
import com.example.ngoun.service.ShopProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ShopProductController {

    private final ShopProductService service;

    @GetMapping("/api/shop-products")
    public List<ShopProduct> getAll() {
        return service.findAll();
    }

    @GetMapping("/api/shop-products/{id}")
    public ResponseEntity<ShopProduct> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/api/shop-products")
    public ShopProduct create(@RequestBody ShopProductRequest req) {
        return service.create(req);
    }

    @PutMapping("/api/shop-products/{id}")
    public ResponseEntity<ShopProduct> update(@PathVariable Long id, @RequestBody ShopProductRequest req) {
        return service.update(id, req)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/shop-products/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
