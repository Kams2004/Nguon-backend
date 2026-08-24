package com.example.ngoun.repository;

import com.example.ngoun.model.ShopProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShopProductRepository extends JpaRepository<ShopProduct, Long> {
    List<ShopProduct> findAllByOrderByFeaturedDescCreatedAtDesc();
}
