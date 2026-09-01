package com.example.ngoun.dto;

import lombok.Data;

@Data
public class VoteOtpRequest {
    private Long voteProfileId;
    private String email;
}
