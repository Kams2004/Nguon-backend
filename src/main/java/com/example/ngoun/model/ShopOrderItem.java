package com.example.ngoun.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "shop_order_items")
@JsonIgnoreProperties({"order"})
public class ShopOrderItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private ShopOrder order;

    // Informational only — no FK constraint. The product may since have
    // changed or been deleted; the fields below are a point-in-time snapshot.
    private Long productId;

    @Column(nullable = false) private String productName;
    @Column(nullable = false) private int qty;
    @Column(nullable = false) private long price;
}
