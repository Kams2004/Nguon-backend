package com.example.ngoun.controller;

import com.example.ngoun.model.Sponsor;
import com.example.ngoun.service.SponsorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sponsors")
@RequiredArgsConstructor
public class SponsorController {
    private final SponsorService sponsorService;
    
    @GetMapping
    public ResponseEntity<List<Sponsor>> getAllSponsors() {
        return ResponseEntity.ok(sponsorService.getAllSponsors());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Sponsor> getSponsorById(@PathVariable Long id) {
        Sponsor sponsor = sponsorService.getSponsorById(id);
        return sponsor != null ? ResponseEntity.ok(sponsor) : ResponseEntity.notFound().build();
    }
    
    @PostMapping
    public ResponseEntity<Sponsor> createSponsor(@RequestBody Sponsor sponsor) {
        return ResponseEntity.ok(sponsorService.createSponsor(sponsor));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Sponsor> updateSponsor(@PathVariable Long id, @RequestBody Sponsor sponsor) {
        Sponsor updated = sponsorService.updateSponsor(id, sponsor);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSponsor(@PathVariable Long id) {
        sponsorService.deleteSponsor(id);
        return ResponseEntity.noContent().build();
    }
}
