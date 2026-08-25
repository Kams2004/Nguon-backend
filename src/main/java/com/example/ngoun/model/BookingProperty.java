package com.example.ngoun.model;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "booking_properties")
public class BookingProperty {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(nullable = false) private String name;
    @Column(nullable = false) private String tagline;
    @Column(columnDefinition = "TEXT", nullable = false) private String description;
    @Column(nullable = false) private String address;
    @Column(nullable = false) private String phone;

    private String whatsapp;
    private String email;
    private String website;
    private String priceFrom;
    private String priceTo;
    private String priceUnit;
    private Integer stars;
    private String cuisine;
    private String openingHours;

    @ElementCollection
    @CollectionTable(name = "booking_property_features", joinColumns = @JoinColumn(name = "property_id"))
    @Column(name = "feature")
    private List<String> features = new ArrayList<>();

    private String accentColor;
    private boolean featured;
    private boolean published;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingMedia> media = new ArrayList<>();

    @Column(updatable = false) private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate  void onUpdate() { updatedAt = LocalDateTime.now(); }

    // Serialized lowercase to match the frontend's "hotel" | "restaurant" type
    // and every comparison built on it — JPA still stores/reads the raw enum
    // name (HOTEL/RESTAURANT) via @Enumerated(EnumType.STRING) above; @JsonValue
    // only changes what goes out over JSON, not persistence.
    public enum Category {
        HOTEL, RESTAURANT;

        @JsonValue
        public String toJson() { return name().toLowerCase(); }
    }
}
