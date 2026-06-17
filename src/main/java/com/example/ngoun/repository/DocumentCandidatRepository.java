package com.example.ngoun.repository;

import com.example.ngoun.model.DocumentCandidat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentCandidatRepository extends JpaRepository<DocumentCandidat, Long> {
    List<DocumentCandidat> findByCandidatId(Long candidatId);
}
