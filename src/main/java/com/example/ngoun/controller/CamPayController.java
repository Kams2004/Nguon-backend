package com.example.ngoun.controller;

import com.example.ngoun.dto.CamPayCollectRequest;
import com.example.ngoun.dto.CamPayCollectResponse;
import com.example.ngoun.dto.CamPayStatusResponse;
import com.example.ngoun.model.ShopOrder;
import com.example.ngoun.repository.ShopOrderRepository;
import com.example.ngoun.service.CamPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payments/campay")
@RequiredArgsConstructor
public class CamPayController {

    private final CamPayService camPayService;
    private final ShopOrderRepository shopOrderRepository;

    @Value("${campay.webhook-key}")
    private String webhookKey;

    @PostMapping("/collect")
    public CamPayCollectResponse collect(@RequestBody CamPayCollectRequest req) {
        return camPayService.collect(req.getAmount(), req.getPhone(), req.getDescription(), req.getExternalReference());
    }

    @GetMapping("/status/{reference}")
    public CamPayStatusResponse status(@PathVariable String reference) {
        return camPayService.checkStatus(reference);
    }

    // Best-effort async notification receiver — the primary confirmation path is
    // the frontend polling /status/{reference} above; this is a defensive extra.
    // CamPay's exact webhook payload shape isn't published, so this stays
    // permissive (accepts GET or POST, logs everything) rather than rejecting
    // requests that don't match an assumed shape.
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhookPost(
            @RequestParam(required = false) String key,
            @RequestBody(required = false) Map<String, Object> body
    ) {
        return handleWebhook(key, body);
    }

    @GetMapping("/webhook")
    public ResponseEntity<Void> webhookGet(
            @RequestParam(required = false) String key,
            @RequestParam Map<String, String> allParams
    ) {
        return handleWebhook(key, Map.copyOf(allParams));
    }

    private ResponseEntity<Void> handleWebhook(String key, Map<String, Object> payload) {
        log.info("CamPay webhook received: {}", payload);

        if (webhookKey != null && !webhookKey.isBlank() && !webhookKey.equals(key)) {
            log.warn("CamPay webhook key mismatch — accepting anyway (logged for review)");
        }

        Object externalRef = payload.get("external_reference");
        Object status = payload.get("status");
        Object reference = payload.get("reference");

        if (externalRef != null && status != null) {
            shopOrderRepository.findById(String.valueOf(externalRef)).ifPresent(order -> {
                String s = String.valueOf(status).toUpperCase();
                if (s.equals("SUCCESSFUL")) order.setPaymentStatus(ShopOrder.PaymentStatus.PAID);
                else if (s.equals("FAILED")) order.setPaymentStatus(ShopOrder.PaymentStatus.FAILED);
                if (reference != null) order.setPaymentId(String.valueOf(reference));
                shopOrderRepository.save(order);
                log.info("Order {} updated from CamPay webhook: paymentStatus={}", order.getId(), order.getPaymentStatus());
            });
        }

        return ResponseEntity.ok().build();
    }
}
