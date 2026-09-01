package com.example.ngoun.service;

import com.example.ngoun.dto.VoteProfileRequest;
import com.example.ngoun.model.VoteProfile;
import com.example.ngoun.repository.VoteProfileRepository;
import com.example.ngoun.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VoteProfileService {

    private final VoteProfileRepository repository;
    private final VoteRepository voteRepository;
    private final PresignedUrlCache urlCache;

    public List<VoteProfile> findAll() {
        return repository.findAllByOrderByCreatedAtAsc().stream().map(this::enrich).toList();
    }

    public List<VoteProfile> findPublished() {
        return repository.findByPublishedTrueOrderByCreatedAtAsc().stream().map(this::enrich).toList();
    }

    public Optional<VoteProfile> findById(Long id) {
        return repository.findById(id).map(this::enrich);
    }

    @Transactional
    public VoteProfile create(VoteProfileRequest req) {
        VoteProfile p = new VoteProfile();
        applyFields(p, req);
        return enrich(repository.save(p));
    }

    @Transactional
    public Optional<VoteProfile> update(Long id, VoteProfileRequest req) {
        return repository.findById(id).map(p -> {
            if (!p.getPhotoUrl().equals(req.getPhotoUrl())) {
                urlCache.invalidate(p.getPhotoUrl());
            }
            applyFields(p, req);
            return enrich(repository.save(p));
        });
    }

    // Deleting a profile that still has (verified or unverified) votes
    // referencing it would otherwise fail with a foreign-key violation —
    // those votes are removed first, in the same transaction.
    @Transactional
    public void delete(Long id) {
        repository.findById(id).ifPresent(p -> {
            urlCache.invalidate(p.getPhotoUrl());
            voteRepository.deleteByVoteProfileId(id);
            repository.delete(p);
        });
    }

    private void applyFields(VoteProfile p, VoteProfileRequest req) {
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setPhotoUrl(req.getPhotoUrl());
        p.setPublished(req.isPublished());
    }

    private VoteProfile enrich(VoteProfile p) {
        p.setPhotoPresignedUrl(urlCache.get(p.getPhotoUrl()));
        return p;
    }
}
