package com.example.ngoun.service;

import com.example.ngoun.dto.CamPayCollectResponse;
import com.example.ngoun.dto.CamPayStatusResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

/**
 * Thin client for the CamPay collections API (MTN Mobile Money / Orange Money).
 * Auth uses the permanent access token from the CamPay dashboard — no separate
 * /api/token/ exchange needed. See https://www.campay.net (Postman docs:
 * https://documenter.getpostman.com/view/2391374/T1LV8PVA) and the official SDKs
 * (e.g. https://github.com/CamPay/campay-python-sdk) for the endpoint contract.
 */
@Slf4j
@Service
public class CamPayService {

    private final RestClient restClient;
    private final String permanentToken;

    public CamPayService(
            @Value("${campay.base-url}") String baseUrl,
            @Value("${campay.permanent-token}") String permanentToken
    ) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.permanentToken = permanentToken;
    }

    @SuppressWarnings("unchecked")
    public CamPayCollectResponse collect(long amount, String phone, String description, String externalReference) {
        Map<String, String> body = Map.of(
                "amount", String.valueOf(amount),
                "currency", "XAF",
                "from", phone,
                "description", description,
                "external_reference", externalReference
        );
        try {
            Map<String, Object> res = restClient.post()
                    .uri("/api/collect/")
                    .header("Authorization", "Token " + permanentToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            return new CamPayCollectResponse(
                    true,
                    str(res.get("reference")),
                    str(res.get("ussd_code")),
                    str(res.get("operator")),
                    null
            );
        } catch (RestClientResponseException e) {
            String message = extractMessage(e);
            log.warn("CamPay collect failed: {} — {}", e.getStatusCode(), message);
            return new CamPayCollectResponse(false, null, null, null, message);
        } catch (Exception e) {
            log.error("CamPay collect error", e);
            return new CamPayCollectResponse(false, null, null, null, "Impossible de contacter le service de paiement.");
        }
    }

    @SuppressWarnings("unchecked")
    public CamPayStatusResponse checkStatus(String reference) {
        try {
            Map<String, Object> res = restClient.get()
                    .uri("/api/transaction/{reference}/", reference)
                    .header("Authorization", "Token " + permanentToken)
                    .retrieve()
                    .body(Map.class);

            return new CamPayStatusResponse(
                    str(res.get("reference")),
                    str(res.get("status")),
                    str(res.get("external_reference")),
                    res.get("amount") != null ? Long.valueOf(str(res.get("amount"))) : null,
                    str(res.get("currency")),
                    str(res.get("operator")),
                    str(res.get("code")),
                    str(res.get("operator_reference")),
                    null
            );
        } catch (RestClientResponseException e) {
            String message = extractMessage(e);
            log.warn("CamPay status check failed for {}: {} — {}", reference, e.getStatusCode(), message);
            return new CamPayStatusResponse(reference, "FAILED", null, null, null, null, null, null, message);
        } catch (Exception e) {
            log.error("CamPay status check error for {}", reference, e);
            return new CamPayStatusResponse(reference, "FAILED", null, null, null, null, null, null, "Impossible de contacter le service de paiement.");
        }
    }

    private String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    @SuppressWarnings("unchecked")
    private String extractMessage(RestClientResponseException e) {
        try {
            Map<String, Object> body = e.getResponseBodyAs(Map.class);
            Object msg = body != null ? body.get("message") : null;
            return msg != null ? String.valueOf(msg) : e.getMessage();
        } catch (Exception parseError) {
            return e.getMessage();
        }
    }
}
