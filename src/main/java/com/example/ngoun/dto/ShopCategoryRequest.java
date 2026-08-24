package com.example.ngoun.dto;

import lombok.Data;

@Data
public class ShopCategoryRequest {
    private String key;
    private String label;
    private String icon;
    private String description;
    private Integer displayOrder;
}
