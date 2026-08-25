package com.example.ngoun.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CamPayCollectResponse {
    private boolean success;
    private String reference;   // use this to poll /status/{reference}
    private String ussdCode;    // shown to the customer, e.g. "*126#" for MTN
    private String operator;    // MTN or ORANGE, as detected by CamPay from the phone number
    private String message;     // present when success = false
}
