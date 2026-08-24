package com.example.ngoun.dto;

import lombok.Data;

@Data
public class ShopOrderStatusUpdateRequest {
    private String status;         // optional — only applied if present
    private String paymentStatus;  // optional — only applied if present
}
