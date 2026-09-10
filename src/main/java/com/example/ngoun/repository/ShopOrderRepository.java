package com.example.ngoun.repository;

import com.example.ngoun.model.ShopOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShopOrderRepository extends JpaRepository<ShopOrder, String> {
    List<ShopOrder> findAllByOrderByCreatedAtDesc();

    @Query("""
            SELECT o FROM ShopOrder o
            WHERE (:status IS NULL OR o.status = :status)
            AND (:paymentStatus IS NULL OR o.paymentStatus = :paymentStatus)
            AND (:q IS NULL OR :q = ''
                 OR LOWER(o.id) LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(o.clientName) LIKE LOWER(CONCAT('%', :q, '%'))
                 OR o.clientPhone LIKE CONCAT('%', :q, '%'))
            """)
    Page<ShopOrder> search(@Param("q") String q, @Param("status") ShopOrder.Status status,
                            @Param("paymentStatus") ShopOrder.PaymentStatus paymentStatus, Pageable pageable);
}
