package com.example.ngoun.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fiches_descriptives")
public class FicheDescriptive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    /** Chemin MinIO vers le fichier PDF */
    @Column(nullable = false)
    private String fichierPdf;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concours_id", nullable = false)
    @JsonIgnore
    private Concours concours;
}
