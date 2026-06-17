package com.example.ngoun.service;

import com.example.ngoun.dto.SouscriptionRequest;
import com.example.ngoun.model.*;
import com.example.ngoun.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CandidatService {

    private final CandidatRepository candidatRepository;
    private final ConcoursRepository concoursRepository;
    private final ParticipationRepository participationRepository;
    private final DocumentCandidatRepository documentCandidatRepository;

    public List<Candidat> findAll() {
        return candidatRepository.findAll();
    }

    public Optional<Candidat> findById(Long id) {
        return candidatRepository.findById(id);
    }

    @Transactional
    public Candidat souscrire(SouscriptionRequest req) {
        // 1. Vérifier que tous les concours existent et sont soumis
        List<Concours> concoursList = new ArrayList<>();
        for (Long concoursId : req.getConcoursIds()) {
            Concours concours = concoursRepository.findById(concoursId)
                    .orElseThrow(() -> new IllegalArgumentException("Concours introuvable : " + concoursId));
            if (!Boolean.TRUE.equals(concours.getSoumis())) {
                throw new IllegalArgumentException("Le concours '" + concours.getSousCategorie() + "' n'est pas encore ouvert aux inscriptions.");
            }
            concoursList.add(concours);
        }

        // 2. Créer ou récupérer le candidat par email
        Candidat candidat = candidatRepository.findByEmail(req.getEmail())
                .orElseGet(Candidat::new);

        candidat.setNomPrenoms(req.getNomPrenoms());
        candidat.setDateNaissance(req.getDateNaissance());
        candidat.setSexe(req.getSexe());
        candidat.setNationalite(req.getNationalite());
        candidat.setProfessionEtablissement(req.getProfessionEtablissement());
        candidat.setVille(req.getVille());
        candidat.setTelephone(req.getTelephone());
        candidat.setWhatsapp(req.getWhatsapp());
        candidat.setEmail(req.getEmail());
        candidat.setUrgenceNom(req.getUrgenceNom());
        candidat.setUrgenceTel(req.getUrgenceTel());
        candidat.setFaitA(req.getFaitA());
        candidat.setDateFait(req.getDateFait());
        candidat.setSignature(req.getSignature());
        if (candidat.getCreatedAt() == null) {
            candidat.setCreatedAt(LocalDateTime.now());
        }
        candidat = candidatRepository.save(candidat);

        // 3. Ajouter les documents
        if (req.getDocuments() != null) {
            for (SouscriptionRequest.DocumentDto docDto : req.getDocuments()) {
                DocumentCandidat doc = new DocumentCandidat();
                doc.setTypeDocument(docDto.getTypeDocument());
                doc.setFichier(docDto.getFichier());
                doc.setCandidat(candidat);
                documentCandidatRepository.save(doc);
            }
        }

        // 4. Enregistrer les participations (avec vérification d'unicité)
        for (Concours concours : concoursList) {
            if (participationRepository.existsByCandidatIdAndConcoursIdAndPeriode(
                    candidat.getId(), concours.getId(), concours.getPeriode())) {
                throw new IllegalArgumentException(
                        "Vous participez déjà au concours '" + concours.getSousCategorie()
                        + "' pour la période " + concours.getPeriode());
            }
            Participation participation = new Participation();
            participation.setCandidat(candidat);
            participation.setConcours(concours);
            participation.setPeriode(concours.getPeriode());
            participation.setInscritLe(LocalDateTime.now());
            participationRepository.save(participation);
        }

        return candidat;
    }

    public List<Participation> getParticipationsByCandidat(Long candidatId) {
        return participationRepository.findByCandidatId(candidatId);
    }

    public List<Participation> getParticipationsByConcours(Long concoursId) {
        return participationRepository.findByConcoursId(concoursId);
    }

    public List<DocumentCandidat> getDocumentsByCandidat(Long candidatId) {
        return documentCandidatRepository.findByCandidatId(candidatId);
    }

    public void delete(Long id) {
        candidatRepository.deleteById(id);
    }
}
