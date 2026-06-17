package com.example.ngoun.controller;

import com.example.ngoun.dto.ConcoursRequest;
import com.example.ngoun.model.Concours;
import com.example.ngoun.model.FicheDescriptive;
import com.example.ngoun.service.ConcoursService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/concours")
@RequiredArgsConstructor
public class ConcoursController {

    private final ConcoursService service;

    /** Public : liste des concours soumis (ouverts aux inscriptions) */
    @GetMapping("/public")
    public List<Concours> getPublic() {
        return service.findSoumis();
    }

    /** Admin : liste de tous les concours */
    @GetMapping
    public List<Concours> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Concours> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Concours> create(@RequestBody ConcoursRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Concours> update(@PathVariable Long id, @RequestBody ConcoursRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    /** Soumettre un concours pour ouvrir les inscriptions */
    @PatchMapping("/{id}/soumettre")
    public ResponseEntity<Concours> soumettre(@PathVariable Long id) {
        return ResponseEntity.ok(service.soumettre(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // --- Fiches descriptives ---

    @GetMapping("/{id}/fiches")
    public List<FicheDescriptive> getFiches(@PathVariable Long id) {
        return service.getFichesByConcours(id);
    }

    @PostMapping("/{id}/fiches")
    public ResponseEntity<FicheDescriptive> addFiche(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        FicheDescriptive fiche = service.ajouterFiche(id, body.get("titre"), body.get("fichierPdf"));
        return ResponseEntity.status(HttpStatus.CREATED).body(fiche);
    }

    @DeleteMapping("/fiches/{ficheId}")
    public ResponseEntity<Void> deleteFiche(@PathVariable Long ficheId) {
        service.supprimerFiche(ficheId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleError(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
