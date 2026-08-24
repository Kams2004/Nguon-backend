package com.example.ngoun.repository;

import com.example.ngoun.model.ShopOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShopOrderRepository extends JpaRepository<ShopOrder, String> {
    List<ShopOrder> findAllByOrderByCreatedAtDesc();
}
