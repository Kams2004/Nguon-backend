package com.example.ngoun.repository;

import com.example.ngoun.model.Concours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConcoursRepository extends JpaRepository<Concours, Long> {
    List<Concours> findBySoumis(Boolean soumis);
    List<Concours> findByPeriode(String periode);
    boolean existsByCategorie(String categorie);
    boolean existsByCategorieAndIdNot(String categorie, Long id);
}
