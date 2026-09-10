package com.example.ngoun.repository;

import com.example.ngoun.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByAuthorityTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String authorityTitle, String content, Pageable pageable);
}
