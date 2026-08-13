package com.example.ngoun.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "booking_media")
@JsonIgnoreProperties({"property"})
public class BookingMedia {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private BookingProperty property;

    @Enumerated(EnumType.STRING)
    private MediaType type;

    @Column(nullable = false)
    private String url;

    private String alt;
    private Integer displayOrder;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String presignedUrl;

    public enum MediaType { IMAGE, VIDEO }
}
