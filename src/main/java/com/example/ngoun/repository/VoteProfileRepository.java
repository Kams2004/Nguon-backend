package com.example.ngoun.repository;

import com.example.ngoun.model.VoteProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoteProfileRepository extends JpaRepository<VoteProfile, Long> {
    List<VoteProfile> findAllByOrderByCreatedAtAsc();
    List<VoteProfile> findByPublishedTrueOrderByCreatedAtAsc();
    Page<VoteProfile> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description, Pageable pageable);
}
