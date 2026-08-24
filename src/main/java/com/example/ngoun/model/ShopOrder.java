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
@Table(name = "shop_orders")
public class ShopOrder {

    // Server-generated human-readable order number (e.g. "ORD-7F3K2A"),
    // set explicitly in ShopOrderService before save — no @GeneratedValue.
    @Id
    private String id;

    @Column(nullable = false) private String clientName;
    @Column(nullable = false) private String clientPhone;
    private String clientEmail;
    @Column(nullable = false) private String address;

    @Column(nullable = false) private long total;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.IDLE;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private String paymentId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShopOrderItem> items = new ArrayList<>();

    @Column(updatable = false) private LocalDateTime createdAt;

    @PrePersist void onCreate() { createdAt = LocalDateTime.now(); }

    public enum Status { PENDING, CONFIRMED, DELIVERED, CANCELLED }
    public enum PaymentStatus { IDLE, PENDING, PROCESSING, PAID, FAILED, REFUNDED }
    public enum PaymentMethod { MOBILE_MONEY, ORANGE_MONEY, CARD, CASH_ON_DELIVERY }
}
