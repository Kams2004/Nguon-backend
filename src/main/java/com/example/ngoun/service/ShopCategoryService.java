package com.example.ngoun.service;

import com.example.ngoun.dto.ShopCategoryRequest;
import com.example.ngoun.model.ShopCategory;
import com.example.ngoun.repository.ShopCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShopCategoryService {

    private final ShopCategoryRepository repository;

    public List<ShopCategory> findAll() {
        return repository.findAllByOrderByDisplayOrderAsc();
    }

    public Optional<ShopCategory> findById(Long id) {
        return repository.findById(id);
    }

    public ShopCategory create(ShopCategoryRequest req) {
        ShopCategory c = new ShopCategory();
        applyFields(c, req);
        return repository.save(c);
    }

    public Optional<ShopCategory> update(Long id, ShopCategoryRequest req) {
        return repository.findById(id).map(c -> {
            applyFields(c, req);
            return repository.save(c);
        });
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void applyFields(ShopCategory c, ShopCategoryRequest req) {
        c.setKey(req.getKey());
        c.setLabel(req.getLabel());
        c.setIcon(req.getIcon());
        c.setDescription(req.getDescription());
        c.setDisplayOrder(req.getDisplayOrder());
    }
}
