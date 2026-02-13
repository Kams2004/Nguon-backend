package com.example.ngoun.repository;

import com.example.ngoun.model.MediaItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaItemRepository extends JpaRepository<MediaItem, Long> {
    List<MediaItem> findByType(String type);
}
