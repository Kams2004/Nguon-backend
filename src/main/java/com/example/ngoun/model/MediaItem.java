package com.example.ngoun.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "media_items")
public class MediaItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type;
    private String url;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    private LocalDateTime createdAt;
    private Boolean published;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String presignedUrl;
}
