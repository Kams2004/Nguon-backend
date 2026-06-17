package com.example.ngoun.repository;

import com.example.ngoun.model.Concours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConcoursRepository extends JpaRepository<Concours, Long> {
    List<Concours> findBySoumis(Boolean soumis);
    List<Concours> findByPeriode(String periode);
}
