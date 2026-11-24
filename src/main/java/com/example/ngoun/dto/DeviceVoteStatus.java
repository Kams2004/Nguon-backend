package com.example.ngoun.dto;

public record DeviceVoteStatus(boolean voted, Long voteProfileId, String profileName) {
    public static DeviceVoteStatus none() { return new DeviceVoteStatus(false, null, null); }
}
