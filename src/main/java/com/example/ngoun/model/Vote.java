package com.example.ngoun.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// One row per email and category (Miss / Mister): a person votes once in each
// category. The unique index on (email, category) and the one-verified-vote-
// per-device-and-category index are created by VoteCategoryMigration. A fresh
// OTP request for an unverified row updates it in place; only a row that
// reaches verified=true counts as a cast vote.
@Data
@NoArgsConstructor
@Entity
@Table(name = "votes")
@JsonIgnoreProperties({"otpCode"})
public class Vote {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_profile_id", nullable = false)
    private VoteProfile voteProfile;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private VoteCategory category;

    @Column(nullable = false)
    private String otpCode;

    @Column(nullable = false)
    private LocalDateTime otpExpiresAt;

    @Column(nullable = false)
    private boolean verified = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime verifiedAt;

    // Random ID kept by the voter's browser; at most one verified vote per
    // device (see VoteService and DeviceVoteIndex).
    @Column(length = 64)
    private String deviceId;

    @PrePersist void onCreate() { createdAt = LocalDateTime.now(); }
}
