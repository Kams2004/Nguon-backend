package com.example.ngoun.repository;

import com.example.ngoun.model.Actuality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActualityRepository extends JpaRepository<Actuality, Long> {
    List<Actuality> findByPublishedTrue();
}
