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
    private final PresignedUrlCache urlCache;

    public List<Concours> findAll() {
        return concoursRepository.findAll().stream().map(this::enrich).toList();
    }

    public List<Concours> findSoumis() {
        return concoursRepository.findBySoumis(true).stream().map(this::enrich).toList();
    }

    public Optional<Concours> findById(Long id) {
        return concoursRepository.findById(id).map(this::enrich);
    }

    public Concours create(ConcoursRequest req) {
        if (concoursRepository.existsByCategorie(req.getCategorie())) {
            throw new IllegalArgumentException("Un concours avec la catégorie '" + req.getCategorie() + "' existe déjà.");
        }
        Concours concours = new Concours();
        concours.setCategorie(req.getCategorie());
        concours.setSousCategorie(req.getSousCategorie());
        concours.setAffiche(req.getAffiche());
        concours.setPeriode(req.getPeriode());
        concours.setSoumis(false);
        concours.setCreatedAt(LocalDateTime.now());
        return enrich(concoursRepository.save(concours));
    }

    public Concours update(Long id, ConcoursRequest req) {
        return concoursRepository.findById(id).map(existing -> {
            if (!existing.getCategorie().equals(req.getCategorie())) {
                if (concoursRepository.existsByCategorieAndIdNot(req.getCategorie(), id)) {
                    throw new IllegalArgumentException("Un concours avec la catégorie '" + req.getCategorie() + "' existe déjà.");
                }
            }
            urlCache.invalidate(existing.getAffiche());
            existing.setCategorie(req.getCategorie());
            existing.setSousCategorie(req.getSousCategorie());
            existing.setAffiche(req.getAffiche());
            existing.setPeriode(req.getPeriode());
            return enrich(concoursRepository.save(existing));
        }).orElseThrow(() -> new IllegalArgumentException("Concours introuvable : " + id));
    }

    public Concours soumettre(Long id) {
        return concoursRepository.findById(id).map(c -> {
            c.setSoumis(true);
            return enrich(concoursRepository.save(c));
        }).orElseThrow(() -> new IllegalArgumentException("Concours introuvable : " + id));
    }

    public Concours unsoumettre(Long id) {
        return concoursRepository.findById(id).map(c -> {
            c.setSoumis(false);
            return enrich(concoursRepository.save(c));
        }).orElseThrow(() -> new IllegalArgumentException("Concours introuvable : " + id));
    }

    @Transactional
    public void delete(Long id) {
        Concours concours = concoursRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Concours introuvable : " + id));
        urlCache.invalidate(concours.getAffiche());
        concoursRepository.delete(concours);
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
        return enrichFiche(ficheDescriptiveRepository.save(fiche));
    }

    public void supprimerFiche(Long ficheId) {
        ficheDescriptiveRepository.findById(ficheId).ifPresent(f -> {
            urlCache.invalidate(f.getFichierPdf());
            ficheDescriptiveRepository.delete(f);
        });
    }

    public List<FicheDescriptive> getFichesByConcours(Long concoursId) {
        return ficheDescriptiveRepository.findByConcoursId(concoursId)
                .stream().map(this::enrichFiche).toList();
    }

    private Concours enrich(Concours c) {
        c.setAffichePresignedUrl(urlCache.get(c.getAffiche()));
        if (c.getFichesDescriptives() != null) {
            c.getFichesDescriptives().forEach(this::enrichFiche);
        }
        return c;
    }

    private FicheDescriptive enrichFiche(FicheDescriptive f) {
        f.setPresignedUrl(urlCache.get(f.getFichierPdf()));
        return f;
    }
}
