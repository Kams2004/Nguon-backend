package com.example.ngoun.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "concours")
public class Concours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String categorie;

    @Column(nullable = false)
    private String sousCategorie;

    /** Chemin MinIO vers l'affiche (image ou PDF) */
    private String affiche;

    @Column(nullable = false)
    private String periode;

    private Boolean soumis = false;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "concours", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FicheDescriptive> fichesDescriptives;

    @JsonIgnore
    @OneToMany(mappedBy = "concours", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participation> participations;
}
