package com.example.ngoun.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "shop_product_media")
@JsonIgnoreProperties({"product"})
public class ShopProductMedia {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ShopProduct product;

    @Enumerated(EnumType.STRING)
    private MediaType type;

    @Column(nullable = false)
    private String url;

    private String alt;
    private Integer displayOrder;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String presignedUrl;

    // Serialized lowercase to match the frontend's "image" | "video" type and
    // every m.type === "image" check built on it.
    public enum MediaType {
        IMAGE, VIDEO;

        @JsonValue
        public String toJson() { return name().toLowerCase(); }
    }
}
