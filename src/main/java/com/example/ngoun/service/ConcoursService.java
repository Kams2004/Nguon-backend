package com.example.ngoun.service;

import com.example.ngoun.dto.ConcoursRequest;
import com.example.ngoun.model.Concours;
import com.example.ngoun.model.FicheDescriptive;
import com.example.ngoun.repository.ConcoursRepository;
import com.example.ngoun.repository.FicheDescriptiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConcoursService {

    private final ConcoursRepository concoursRepository;
    private final FicheDescriptiveRepository ficheDescriptiveRepository;

    public List<Concours> findAll() {
        return concoursRepository.findAll();
    }

    public List<Concours> findSoumis() {
        return concoursRepository.findBySoumis(true);
    }

    public Optional<Concours> findById(Long id) {
        return concoursRepository.findById(id);
    }

    public Concours create(ConcoursRequest req) {
        Concours concours = new Concours();
        concours.setCategorie(req.getCategorie());
        concours.setSousCategorie(req.getSousCategorie());
        concours.setAffiche(req.getAffiche());
        concours.setPeriode(req.getPeriode());
        concours.setSoumis(false);
        concours.setCreatedAt(LocalDateTime.now());
        return concoursRepository.save(concours);
    }

    public Concours update(Long id, ConcoursRequest req) {
        return concoursRepository.findById(id).map(existing -> {
            existing.setCategorie(req.getCategorie());
            existing.setSousCategorie(req.getSousCategorie());
            existing.setAffiche(req.getAffiche());
            existing.setPeriode(req.getPeriode());
            return concoursRepository.save(existing);
        }).orElseThrow(() -> new IllegalArgumentException("Concours introuvable : " + id));
    }

    public Concours soumettre(Long id) {
        return concoursRepository.findById(id).map(concours -> {
            concours.setSoumis(true);
            return concoursRepository.save(concours);
        }).orElseThrow(() -> new IllegalArgumentException("Concours introuvable : " + id));
    }

    public void delete(Long id) {
        concoursRepository.deleteById(id);
    }

    // --- Fiches descriptives ---

    @Transactional
    public FicheDescriptive ajouterFiche(Long concoursId, String titre, String fichierPdf) {
        Concours concours = concoursRepository.findById(concoursId)
                .orElseThrow(() -> new IllegalArgumentException("Concours introuvable : " + concoursId));
        FicheDescriptive fiche = new FicheDescriptive();
        fiche.setTitre(titre);
        fiche.setFichierPdf(fichierPdf);
        fiche.setConcours(concours);
        return ficheDescriptiveRepository.save(fiche);
    }

    public void supprimerFiche(Long ficheId) {
        ficheDescriptiveRepository.deleteById(ficheId);
    }

    public List<FicheDescriptive> getFichesByConcours(Long concoursId) {
        return ficheDescriptiveRepository.findByConcoursId(concoursId);
    }
}
