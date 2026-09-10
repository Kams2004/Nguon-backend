package com.example.ngoun.repository;

import com.example.ngoun.model.ShopProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShopProductRepository extends JpaRepository<ShopProduct, Long> {
    List<ShopProduct> findAllByOrderByFeaturedDescCreatedAtDesc();
    List<ShopProduct> findByPublishedTrueOrderByFeaturedDescCreatedAtDesc();

    @Query("""
            SELECT p FROM ShopProduct p
            WHERE (:category IS NULL OR :category = '' OR p.category = :category)
            AND (:q IS NULL OR :q = ''
                 OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(p.seller) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<ShopProduct> search(@Param("q") String q, @Param("category") String category, Pageable pageable);
}
