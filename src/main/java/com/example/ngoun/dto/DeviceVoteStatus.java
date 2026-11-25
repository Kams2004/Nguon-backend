package com.example.ngoun.dto;

import com.example.ngoun.model.VoteCategory;

import java.util.List;

/** Votes already cast from a device, one per category. The first three fields keep older pages working. */
public record DeviceVoteStatus(boolean voted, Long voteProfileId, String profileName, List<CategoryVote> votes) {
    public record CategoryVote(VoteCategory category, Long voteProfileId, String profileName) {}

    public static DeviceVoteStatus none() { return new DeviceVoteStatus(false, null, null, List.of()); }
}
