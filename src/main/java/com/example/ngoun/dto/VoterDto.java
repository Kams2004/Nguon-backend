package com.example.ngoun.dto;

import java.time.LocalDateTime;

public record VoterDto(String email, LocalDateTime verifiedAt) {}
