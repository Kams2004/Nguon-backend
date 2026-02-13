package com.example.ngoun.service;

import com.example.ngoun.model.Message;
import com.example.ngoun.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
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
