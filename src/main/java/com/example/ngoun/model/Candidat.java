package com.example.ngoun.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "candidats")
public class Candidat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Informations personnelles ---
    @Column(nullable = false)
    private String nomPrenoms;

    @Column(nullable = false)
    private LocalDate dateNaissance;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Sexe sexe;

    @Column(nullable = false)
    private String nationalite;

    private String professionEtablissement;
    private String ville;

    @Column(nullable = false)
    private String telephone;

    private String whatsapp;

    @Column(nullable = false)
    private String email;

    // --- Personne à contacter en cas d'urgence ---
    private String urgenceNom;
    private String urgenceTel;

    // --- Déclaration ---
    private String faitA;
    private LocalDate dateFait;

    /** Signature : nom et prénom ressaisis manuellement par le candidat */
    @Column(nullable = false)
    private String signatureNom;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "candidat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentCandidat> documents;

    @OneToMany(mappedBy = "candidat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participation> participations;

    public enum Sexe {
        MASCULIN, FEMININ;

        @JsonCreator
        public static Sexe fromString(String value) {
            if (value == null) throw new IllegalArgumentException("Sexe value cannot be null");
            // Normalize accented characters (e.g. FÉMININ → FEMININ)
            String normalized = java.text.Normalizer.normalize(value.toUpperCase(), java.text.Normalizer.Form.NFD)
                    .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
            return Sexe.valueOf(normalized);
        }
    }
}
