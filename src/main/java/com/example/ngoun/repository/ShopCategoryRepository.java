package com.example.ngoun.repository;

import com.example.ngoun.model.ShopCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShopCategoryRepository extends JpaRepository<ShopCategory, Long> {
    List<ShopCategory> findAllByOrderByDisplayOrderAsc();
    Optional<ShopCategory> findByKey(String key);
    boolean existsByKey(String key);
}
