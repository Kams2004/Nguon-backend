package com.example.ngoun.dto;

import lombok.Data;

@Data
public class BookingMediaRequest {
    private String type;
    private String url;
    private String alt;
    private Integer displayOrder;
}
