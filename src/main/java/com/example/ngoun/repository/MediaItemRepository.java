package com.example.ngoun.repository;

import com.example.ngoun.model.MediaItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaItemRepository extends JpaRepository<MediaItem, Long> {
    List<MediaItem> findByType(String type);
    Page<MediaItem> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<MediaItem> findByTypeAndTitleContainingIgnoreCase(String type, String title, Pageable pageable);
    Page<MediaItem> findByType(String type, Pageable pageable);
}
