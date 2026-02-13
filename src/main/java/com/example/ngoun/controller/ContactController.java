package com.example.ngoun.controller;

import com.example.ngoun.model.Contact;
import com.example.ngoun.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {
    private final ContactService service;

    @PostMapping
    public Contact create(@RequestBody Contact contact) {
        return service.create(contact);
    }

    @GetMapping
    public List<Contact> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Contact getById(@PathVariable Long id) {
        return service.findById(id).orElse(null);
    }

    @PostMapping("/{id}/respond")
    public ResponseEntity<String> respond(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String responseMessage = request.get("message");
        service.respondToContact(id, responseMessage);
        return ResponseEntity.ok("Response sent successfully");
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
