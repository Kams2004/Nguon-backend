package com.example.ngoun.service;

import com.example.ngoun.model.Contact;
import com.example.ngoun.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContactService {
    private final ContactRepository repository;
    private final EmailService emailService;

    public List<Contact> findAll() {
        return repository.findAll();
    }

    public Optional<Contact> findById(Long id) {
        return repository.findById(id);
    }

    public Contact create(Contact contact) {
        contact.setCreatedAt(LocalDateTime.now());
        contact.setResponded(false);
        return repository.save(contact);
    }

    public void respondToContact(Long id, String responseMessage) {
        repository.findById(id).ifPresent(contact -> {
            emailService.sendEmail(contact.getEmail(), "Response to your message", responseMessage);
            contact.setResponded(true);
            repository.save(contact);
        });
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
