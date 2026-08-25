package com.example.ngoun.dto;

import lombok.Data;

@Data
public class CamPayCollectRequest {
    private long amount;             // FCFA, whole number
    private String phone;            // payer's mobile money number, e.g. "+2376xxxxxxxx"
    private String description;
    private String externalReference; // our own tracking id, shown back to us in status checks
}
