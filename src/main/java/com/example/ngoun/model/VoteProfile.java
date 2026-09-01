package com.example.ngoun.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "vote_profiles")
public class VoteProfile {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String photoUrl;

    @Column(nullable = false)
    private boolean published = true;

    @Column(nullable = false)
    private int voteCount = 0;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String photoPresignedUrl;

    @PrePersist void onCreate() { createdAt = LocalDateTime.now(); }
}
