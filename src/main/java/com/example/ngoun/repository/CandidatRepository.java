package com.example.ngoun.repository;

import com.example.ngoun.model.Candidat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CandidatRepository extends JpaRepository<Candidat, Long> {
    Optional<Candidat> findByEmail(String email);

    // Admin list: optional free-text search (name/email/ville) combined with
    // an optional "only candidates registered for this concours" filter —
    // both null/blank means "everyone".
    @Query("""
            SELECT DISTINCT c FROM Candidat c
            LEFT JOIN c.participations p
            WHERE (:concoursId IS NULL OR p.concours.id = :concoursId)
            AND (:q IS NULL OR :q = ''
                 OR LOWER(c.nomPrenoms) LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(c.email) LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(c.ville) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<Candidat> search(@Param("q") String q, @Param("concoursId") Long concoursId, Pageable pageable);

    // Same filter, unpaged — backs the "export all matching" action.
    @Query("""
            SELECT DISTINCT c FROM Candidat c
            LEFT JOIN c.participations p
            WHERE (:concoursId IS NULL OR p.concours.id = :concoursId)
            AND (:q IS NULL OR :q = ''
                 OR LOWER(c.nomPrenoms) LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(c.email) LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(c.ville) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    List<Candidat> searchAll(@Param("q") String q, @Param("concoursId") Long concoursId);
}
