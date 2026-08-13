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
@Table(name = "activities", uniqueConstraints = {
    @UniqueConstraint(columnNames = "name"),
    @UniqueConstraint(columnNames = "displayOrder")
})
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String image;

    @Column(name = "displayOrder", unique = true, nullable = false)
    private Integer displayOrder;

    private LocalDateTime createdAt;
    private Boolean published;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String presignedUrl;
}
