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
                    toLong(res.get("amount")),
                    str(res.get("currency")),
                    str(res.get("operator")),
                    str(res.get("code")),
                    str(res.get("operator_reference")),
                    null
            );
        } catch (RestClientResponseException e) {
            // A failed status *check* (network hiccup, transient 5xx from CamPay,
            // etc.) is not the same as CamPay telling us the payment itself
            // failed — that only ever comes back as a clean 200 with
            // status:"FAILED" in the try block above. Reporting "FAILED" here
            // would let one bad poll wrongly abort a payment that's still
            // being confirmed on the customer's phone, so we report "PENDING"
            // instead and let the frontend's poll loop (with its own timeout)
            // simply retry on the next interval.
            String message = extractMessage(e);
            log.warn("CamPay status check failed for {}: {} — {} (reporting PENDING, will retry)", reference, e.getStatusCode(), message);
            return new CamPayStatusResponse(reference, "PENDING", null, null, null, null, null, null, message);
        } catch (Exception e) {
            log.error("CamPay status check error for {} (reporting PENDING, will retry)", reference, e);
            return new CamPayStatusResponse(reference, "PENDING", null, null, null, null, null, null, "Impossible de contacter le service de paiement.");
        }
    }

    private String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    // CamPay's "amount" can come back as a JSON integer or a decimal (e.g. 12.0)
    // depending on the transaction — Jackson then hands us an Integer/Long/Double
    // interchangeably. Long.valueOf("12.0") throws, which was silently turning
    // every successful status check into a false "can't reach payment service"
    // error. Handle any Number, and fall back gracefully instead of throwing.
    private Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.longValue();
        try {
            return (long) Double.parseDouble(String.valueOf(o));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private String extractMessage(RestClientResponseException e) {
        try {
            Map<String, Object> body = e.getResponseBodyAs(Map.class);
            // CamPay uses "message" on collect/status errors but "detail" on auth
            // errors (e.g. an invalid token) — check both before falling back.
            Object msg = body != null ? (body.get("message") != null ? body.get("message") : body.get("detail")) : null;
            return msg != null ? String.valueOf(msg) : e.getMessage();
        } catch (Exception parseError) {
            return e.getMessage();
        }
    }
}
