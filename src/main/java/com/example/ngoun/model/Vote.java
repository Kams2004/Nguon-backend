package com.example.ngoun.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// One row per email, ever — the unique constraint on `email` is what enforces
// "one vote total across the whole contest" (not per-profile). A fresh OTP
// request for an unverified row updates it in place rather than inserting a
// new one; only a row that reaches verified=true counts as a cast vote.
@Data
@NoArgsConstructor
@Entity
@Table(name = "votes", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@JsonIgnoreProperties({"otpCode"})
public class Vote {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_profile_id", nullable = false)
    private VoteProfile voteProfile;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String otpCode;

    @Column(nullable = false)
    private LocalDateTime otpExpiresAt;

    @Column(nullable = false)
    private boolean verified = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime verifiedAt;

    @PrePersist void onCreate() { createdAt = LocalDateTime.now(); }
}
