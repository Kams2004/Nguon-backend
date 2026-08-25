package com.example.ngoun.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CamPayStatusResponse {
    private String reference;
    private String status;           // PENDING | SUCCESSFUL | FAILED
    private String externalReference;
    private Long amount;
    private String currency;
    private String operator;
    private String code;
    private String operatorReference;
    private String message;          // present on error
}
