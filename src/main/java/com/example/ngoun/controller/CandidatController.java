package com.example.ngoun.controller;

import com.example.ngoun.dto.SouscriptionRequest;
import com.example.ngoun.model.Candidat;
import com.example.ngoun.model.DocumentCandidat;
import com.example.ngoun.model.Participation;
import com.example.ngoun.service.CandidatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candidats")
@RequiredArgsConstructor
public class CandidatController {

    private final CandidatService service;

    /** Admin : liste de tous les candidats */
    @GetMapping
    public List<Candidat> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Candidat> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Public : inscription d'un candidat à un ou plusieurs concours */
    @PostMapping("/souscrire")
    public ResponseEntity<Candidat> souscrire(@RequestBody SouscriptionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.souscrire(req));
    }

    @GetMapping("/{id}/participations")
    public List<Participation> getParticipations(@PathVariable Long id) {
        return service.getParticipationsByCandidat(id);
    }

    @GetMapping("/{id}/documents")
    public List<DocumentCandidat> getDocuments(@PathVariable Long id) {
        return service.getDocumentsByCandidat(id);
    }

    /** Admin : liste des candidats inscrits à un concours */
    @GetMapping("/concours/{concoursId}")
    public List<Participation> getCandidatsByConcours(@PathVariable Long concoursId) {
        return service.getParticipationsByConcours(concoursId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleError(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
