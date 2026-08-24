package com.example.ngoun.controller;

import com.example.ngoun.dto.ShopOrderRequest;
import com.example.ngoun.dto.ShopOrderStatusUpdateRequest;
import com.example.ngoun.model.ShopOrder;
import com.example.ngoun.service.ShopOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ShopOrderController {

    private final ShopOrderService service;

    @PostMapping("/api/shop-orders")
    public ShopOrder create(@RequestBody ShopOrderRequest req) {
        return service.create(req);
    }

    @GetMapping("/api/shop-orders")
    public List<ShopOrder> getAll() {
        return service.findAll();
    }

    @GetMapping("/api/shop-orders/{id}")
    public ResponseEntity<ShopOrder> getById(@PathVariable String id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/api/shop-orders/{id}/status")
    public ResponseEntity<ShopOrder> updateStatus(@PathVariable String id, @RequestBody ShopOrderStatusUpdateRequest req) {
        return service.updateStatus(id, req)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
