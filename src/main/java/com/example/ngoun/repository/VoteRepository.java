package com.example.ngoun.repository;

import com.example.ngoun.model.Vote;
import com.example.ngoun.model.VoteCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    List<Vote> findAllByEmail(String email);
    Optional<Vote> findByEmailAndCategory(String email, VoteCategory category);
    List<Vote> findByVoteProfileIdAndVerifiedTrueOrderByVerifiedAtDesc(Long voteProfileId);
    long countByVoteProfileIdAndVerifiedTrue(Long voteProfileId);
    void deleteByVoteProfileId(Long voteProfileId);
    boolean existsByDeviceIdAndCategoryAndVerifiedTrue(String deviceId, VoteCategory category);
    List<Vote> findByDeviceIdAndVerifiedTrue(String deviceId);
}
