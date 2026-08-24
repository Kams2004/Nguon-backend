package com.example.ngoun.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "shop_products")
public class ShopProduct {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Plain string key into ShopCategory.key — deliberately NOT a JPA relation.
    // Deleting a category must leave existing products' category value untouched.
    @Column(nullable = false)
    private String category;

    @Column(nullable = false) private String name;
    @Column(nullable = false) private String tagline;
    @Column(columnDefinition = "TEXT", nullable = false) private String description;

    @Column(nullable = false) private long price;
    private Long comparePrice;
    private String unit;

    @Column(nullable = false) private boolean inStock;
    private Integer stockQty;

    @Column(nullable = false) private String seller;
    @Column(nullable = false) private String sellerLocation;
    private String phone;
    private String whatsapp;

    @ElementCollection
    @CollectionTable(name = "shop_product_tags", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    private boolean featured;
    private String badge;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShopProductMedia> media = new ArrayList<>();

    @Column(updatable = false) private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate  void onUpdate() { updatedAt = LocalDateTime.now(); }
}
