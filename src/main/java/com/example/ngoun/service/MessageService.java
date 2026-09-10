package com.example.ngoun.service;

import com.example.ngoun.model.Message;
import com.example.ngoun.repository.MessageRepository;
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
public class MessageService {
    private final MessageRepository repository;

    public List<Message> findAll() {
        return repository.findAll();
    }

    public Page<Message> findPaged(int page, int size, String search) {
        PageRequest request = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        String q = search == null ? "" : search.trim();
        return q.isEmpty() ? repository.findAll(request) : repository.findByAuthorityTitleContainingIgnoreCaseOrContentContainingIgnoreCase(q, q, request);
    }

    public Optional<Message> findById(Long id) {
        return repository.findById(id);
    }

    public Message create(Message message) {
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());
        return repository.save(message);
    }

    public Message update(Long id, Message message) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setAuthorityTitle(message.getAuthorityTitle());
                    existing.setContent(message.getContent());
                    existing.setUpdatedAt(LocalDateTime.now());
                    existing.setPublished(message.getPublished());
                    return repository.save(existing);
                }).orElse(null);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
