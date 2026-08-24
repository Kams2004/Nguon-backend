package com.example.ngoun.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "shop_categories")
public class ShopCategory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String key;

    @Column(nullable = false)
    private String label;

    private String icon;
    private String description;
    private Integer displayOrder;
}
