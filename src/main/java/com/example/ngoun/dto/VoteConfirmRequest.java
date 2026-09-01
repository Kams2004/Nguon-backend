package com.example.ngoun.dto;

import lombok.Data;

@Data
public class VoteConfirmRequest {
    private String email;
    private String otp;
}
