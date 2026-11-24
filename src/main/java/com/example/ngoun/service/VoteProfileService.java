package com.example.ngoun.service;

import com.example.ngoun.dto.VoteProfileRequest;
import com.example.ngoun.model.VoteProfile;
import com.example.ngoun.repository.VoteProfileRepository;
import com.example.ngoun.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
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

    public Page<VoteProfile> findPaged(int page, int size, String search) {
        PageRequest request = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        String q = fold(search);
        if (q.isEmpty()) return repository.findAll(request).map(this::enrich);
        // Names are often pasted from social media in Unicode "mathematical
        // bold" letters, which a SQL LIKE never matches, so the (small) list is
        // folded and filtered here instead.
        List<VoteProfile> matches = repository.findAllByOrderByCreatedAtAsc().stream()
                .filter(p -> fold(p.getName()).contains(q) || fold(p.getDescription()).contains(q))
                .toList();
        int from = Math.min((int) request.getOffset(), matches.size());
        int to = Math.min(from + request.getPageSize(), matches.size());
        return new PageImpl<>(matches.subList(from, to), request, matches.size()).map(this::enrich);
    }

    private static String fold(String text) {
        if (text == null) return "";
        return Normalizer.normalize(text, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[\\u200B-\\u200D\\uFEFF]", "")
                .toLowerCase(Locale.ROOT)
                .trim();
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
