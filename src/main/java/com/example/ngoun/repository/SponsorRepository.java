package com.example.ngoun.repository;

import com.example.ngoun.model.Sponsor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SponsorRepository extends JpaRepository<Sponsor, Long> {
    Page<Sponsor> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
