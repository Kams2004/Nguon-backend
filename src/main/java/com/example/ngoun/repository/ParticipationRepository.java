package com.example.ngoun.repository;

import com.example.ngoun.model.Participation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    boolean existsByCandidatIdAndConcoursIdAndPeriode(Long candidatId, Long concoursId, String periode);
    List<Participation> findByCandidatId(Long candidatId);
    List<Participation> findByConcoursId(Long concoursId);
}
