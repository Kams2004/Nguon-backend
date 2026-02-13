package com.example.ngoun.service;

import com.example.ngoun.model.Role;
import com.example.ngoun.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository repository;

    public List<Role> findAll() {
        return repository.findAll();
    }

    public Optional<Role> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<Role> findByName(String name) {
        return repository.findByName(name);
    }

    public Role create(Role role) {
        role.setCreatedAt(LocalDateTime.now());
        return repository.save(role);
    }

    public Role update(Long id, Role role) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setName(role.getName());
                    existing.setDescription(role.getDescription());
                    return repository.save(existing);
                }).orElse(null);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
