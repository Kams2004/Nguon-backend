package com.example.ngoun.dto;

public record VoteOtpResponse(boolean success, String message) {
    public static VoteOtpResponse ok() { return new VoteOtpResponse(true, null); }
    public static VoteOtpResponse fail(String message) { return new VoteOtpResponse(false, message); }
}
