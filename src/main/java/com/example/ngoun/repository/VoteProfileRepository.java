package com.example.ngoun.repository;

import com.example.ngoun.model.VoteProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoteProfileRepository extends JpaRepository<VoteProfile, Long> {
    List<VoteProfile> findAllByOrderByCreatedAtAsc();
    List<VoteProfile> findByPublishedTrueOrderByCreatedAtAsc();
}
