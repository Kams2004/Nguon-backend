package com.example.ngoun.service;

import com.example.ngoun.model.Sponsor;
import com.example.ngoun.repository.SponsorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SponsorService {
    private final SponsorRepository sponsorRepository;
    private final PresignedUrlCache urlCache;

    public List<Sponsor> getAllSponsors() {
        return sponsorRepository.findAll().stream().map(this::enrich).toList();
    }

    public Sponsor getSponsorById(Long id) {
        return sponsorRepository.findById(id).map(this::enrich).orElse(null);
    }

    public Sponsor createSponsor(Sponsor sponsor) {
        return enrich(sponsorRepository.save(sponsor));
    }

    public Sponsor updateSponsor(Long id, Sponsor sponsor) {
        return sponsorRepository.findById(id).map(existing -> {
            urlCache.invalidate(existing.getImage());
            existing.setName(sponsor.getName());
            existing.setImage(sponsor.getImage());
            return enrich(sponsorRepository.save(existing));
        }).orElse(null);
    }

    public void deleteSponsor(Long id) {
        sponsorRepository.findById(id).ifPresent(s -> {
            urlCache.invalidate(s.getImage());
            sponsorRepository.delete(s);
        });
    }

    private Sponsor enrich(Sponsor s) {
        s.setPresignedUrl(urlCache.get(s.getImage()));
        return s;
    }
}
