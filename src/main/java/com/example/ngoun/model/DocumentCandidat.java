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
@Table(name = "documents_candidat")
public class DocumentCandidat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TypeDocument typeDocument;

    /** Chemin MinIO vers le fichier (PDF ou image) */
    @Column(nullable = false)
    private String fichier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidat_id", nullable = false)
    @JsonIgnore
    private Candidat candidat;

    public enum TypeDocument {
        CNI_OU_ACTE_NAISSANCE,
        CV_OU_PROJET,
        AUTORISATION_PARENTALE,
        CERTIFICAT_MEDICAL,
        AUTRE
    }
}
