package com.example.ngoun.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ShopOrderRequest {
    private String clientName;
    private String clientPhone;
    private String clientEmail;
    private String address;
    private String paymentMethod;   // MOBILE_MONEY | ORANGE_MONEY | CARD | CASH_ON_DELIVERY
    private String paymentStatus;   // set by the caller (e.g. PAID once the payment stub succeeds)
    private String paymentId;
    private List<ItemRef> items = new ArrayList<>();

    @Data
    public static class ItemRef {
        private Long productId;
        private String productName;
        private int qty;
        private long price;
    }
}
