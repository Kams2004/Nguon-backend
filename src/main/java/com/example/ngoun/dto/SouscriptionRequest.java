package com.example.ngoun.dto;

import com.example.ngoun.model.Candidat.Sexe;
import com.example.ngoun.model.DocumentCandidat.TypeDocument;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class SouscriptionRequest {

    // Informations personnelles
    private String nomPrenoms;
    private LocalDate dateNaissance;
    private Sexe sexe;
    private String nationalite;
    private String professionEtablissement;
    private String ville;
    private String telephone;
    private String whatsapp;
    private String email;

    // Personne à contacter en cas d'urgence
    private String urgenceNom;
    private String urgenceTel;

    // Déclaration
    private String faitA;
    private LocalDate dateFait;
    /** Nom et prénom ressaisis manuellement — fait office de signature */
    private String signatureNom;

    // Concours choisis (liste d'IDs)
    private List<Long> concoursIds;

    // Documents fournis
    private List<DocumentDto> documents;

    @Data
    public static class DocumentDto {
        private TypeDocument typeDocument;
        private String fichier;
    }
}
