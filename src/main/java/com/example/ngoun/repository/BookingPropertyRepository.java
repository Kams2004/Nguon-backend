package com.example.ngoun.repository;

import com.example.ngoun.model.BookingProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingPropertyRepository extends JpaRepository<BookingProperty, Long> {
    List<BookingProperty> findByPublishedTrueOrderByFeaturedDescCreatedAtDesc();
    List<BookingProperty> findAllByOrderByFeaturedDescCreatedAtDesc();
}
