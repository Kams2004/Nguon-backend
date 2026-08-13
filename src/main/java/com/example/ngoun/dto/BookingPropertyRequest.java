package com.example.ngoun.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BookingPropertyRequest {
    private String category;
    private String name;
    private String tagline;
    private String description;
    private String address;
    private String phone;
    private String whatsapp;
    private String email;
    private String website;
    private String priceFrom;
    private String priceTo;
    private String priceUnit;
    private Integer stars;
    private String cuisine;
    private String openingHours;
    private List<String> features = new ArrayList<>();
    private String accentColor;
    private boolean featured;
    private boolean published;
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
