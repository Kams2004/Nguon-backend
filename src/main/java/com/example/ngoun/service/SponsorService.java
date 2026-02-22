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
    
    public List<Sponsor> getAllSponsors() {
        return sponsorRepository.findAll();
    }
    
    public Sponsor getSponsorById(Long id) {
        return sponsorRepository.findById(id).orElse(null);
    }
    
    public Sponsor createSponsor(Sponsor sponsor) {
        return sponsorRepository.save(sponsor);
    }
    
    public Sponsor updateSponsor(Long id, Sponsor sponsor) {
        Sponsor existing = sponsorRepository.findById(id).orElse(null);
        if (existing != null) {
            existing.setName(sponsor.getName());
            existing.setImage(sponsor.getImage());
            return sponsorRepository.save(existing);
        }
        return null;
    }
    
    public void deleteSponsor(Long id) {
        sponsorRepository.deleteById(id);
    }
}
