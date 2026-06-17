package com.example.ngoun.dto;

import lombok.Data;

@Data
public class ConcoursRequest {
    private String categorie;
    private String sousCategorie;
    private String affiche;
    private String periode;
}
