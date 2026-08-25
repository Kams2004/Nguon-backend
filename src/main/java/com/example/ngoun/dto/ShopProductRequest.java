package com.example.ngoun.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ShopProductRequest {
    private String category;
    private String name;
    private String tagline;
    private String description;
    private long price;
    private Long comparePrice;
    private String unit;
    private boolean inStock;
    private Integer stockQty;
    private String seller;
    private String sellerLocation;
    private String phone;
    private String whatsapp;
    private List<String> tags = new ArrayList<>();
    private boolean featured;
    private String badge;
    private boolean published = true;
    private List<MediaRef> media = new ArrayList<>();

    @Data
    public static class MediaRef {
        private Long id;       // existing media — keep
        private String type;   // new media
        private String url;
        private String alt;
        private Integer displayOrder;
    }
}
