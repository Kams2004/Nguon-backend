package com.example.ngoun.repository;

import com.example.ngoun.model.BookingMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingMediaRepository extends JpaRepository<BookingMedia, Long> {
    List<BookingMedia> findByPropertyId(Long propertyId);
}
