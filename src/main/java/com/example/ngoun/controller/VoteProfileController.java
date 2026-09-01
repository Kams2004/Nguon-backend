package com.example.ngoun.controller;

import com.example.ngoun.dto.VoteProfileRequest;
import com.example.ngoun.dto.VoterDto;
import com.example.ngoun.model.VoteProfile;
import com.example.ngoun.service.VoteProfileService;
import com.example.ngoun.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class VoteProfileController {

    private final VoteProfileService service;
    private final VoteService voteService;

    @GetMapping("/api/vote-profiles")
    public List<VoteProfile> getPublished() {
        return service.findPublished();
    }

    @GetMapping("/api/vote-profiles/admin")
    public List<VoteProfile> getAll() {
        return service.findAll();
    }

    @GetMapping("/api/vote-profiles/{id}")
    public ResponseEntity<VoteProfile> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/api/vote-profiles")
    public VoteProfile create(@RequestBody VoteProfileRequest req) {
        return service.create(req);
    }

    @PutMapping("/api/vote-profiles/{id}")
    public ResponseEntity<VoteProfile> update(@PathVariable Long id, @RequestBody VoteProfileRequest req) {
        return service.update(id, req)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/vote-profiles/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Admin-only "who voted for this profile" list — the same table that
    // keeps a given email from voting for it (or anywhere else) twice.
    @GetMapping("/api/vote-profiles/{id}/voters")
    public List<VoterDto> voters(@PathVariable Long id) {
        return voteService.votersOf(id);
    }
}
