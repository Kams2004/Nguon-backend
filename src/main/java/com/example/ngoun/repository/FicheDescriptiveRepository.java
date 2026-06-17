package com.example.ngoun.repository;

import com.example.ngoun.model.FicheDescriptive;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FicheDescriptiveRepository extends JpaRepository<FicheDescriptive, Long> {
    List<FicheDescriptive> findByConcoursId(Long concoursId);
}
