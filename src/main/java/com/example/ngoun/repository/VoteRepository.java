package com.example.ngoun.repository;

import com.example.ngoun.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByEmail(String email);
    List<Vote> findByVoteProfileIdAndVerifiedTrueOrderByVerifiedAtDesc(Long voteProfileId);
    long countByVoteProfileIdAndVerifiedTrue(Long voteProfileId);
    void deleteByVoteProfileId(Long voteProfileId);
}
