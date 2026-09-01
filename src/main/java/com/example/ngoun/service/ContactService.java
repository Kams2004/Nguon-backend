package com.example.ngoun.service;

import com.example.ngoun.model.Contact;
import com.example.ngoun.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    public Page<Contact> findPaged(int page, int size, String search) {
        PageRequest request = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        String q = search == null ? "" : search.trim();
        return q.isEmpty()
                ? repository.findAll(request)
                : repository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q, request);
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
