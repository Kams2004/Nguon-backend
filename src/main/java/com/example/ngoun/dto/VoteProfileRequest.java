package com.example.ngoun.dto;

import lombok.Data;

@Data
public class VoteProfileRequest {
    private String name;
    private String description;
    private String photoUrl;
    private boolean published = true;
}
