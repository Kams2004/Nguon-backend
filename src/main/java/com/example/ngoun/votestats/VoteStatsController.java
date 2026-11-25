package com.example.ngoun.votestats;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class VoteStatsController {

    private final VoteStatsService service;

    @GetMapping("/api/vote-profiles/admin/stats")
    public VoteStatsService.Overview overview() {
        return service.overview();
    }

    @GetMapping("/api/vote-profiles/admin/{id}/stats")
    public ResponseEntity<VoteStatsService.CandidateStats> candidate(@PathVariable Long id) {
        return ResponseEntity.of(service.candidate(id));
    }
}
