package com.example.ngoun.repository;

import com.example.ngoun.model.BookingProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingPropertyRepository extends JpaRepository<BookingProperty, Long> {
    List<BookingProperty> findByPublishedTrueOrderByFeaturedDescCreatedAtDesc();
    List<BookingProperty> findAllByOrderByFeaturedDescCreatedAtDesc();

    @Query("""
            SELECT p FROM BookingProperty p
            WHERE (:category IS NULL OR p.category = :category)
            AND (:q IS NULL OR :q = ''
                 OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(p.address) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<BookingProperty> search(@Param("q") String q, @Param("category") BookingProperty.Category category, Pageable pageable);
}
