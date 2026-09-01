package com.example.ngoun.dto;

public record VoteConfirmResponse(boolean success, String message, String profileName) {
    public static VoteConfirmResponse ok(String profileName) { return new VoteConfirmResponse(true, null, profileName); }
    public static VoteConfirmResponse fail(String message) { return new VoteConfirmResponse(false, message, null); }
}
