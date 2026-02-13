package com.example.ngoun.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "manifestation_sites")
public class ManifestationSite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String image;
    
    @Column(unique = true, nullable = false)
    private String townTitle;
    
    @ElementCollection
    @CollectionTable(name = "sub_town_titles", joinColumns = @JoinColumn(name = "site_id"))
    @Column(name = "sub_town_title")
    private List<String> subTownTitles;
}
