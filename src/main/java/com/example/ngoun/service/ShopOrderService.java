package com.example.ngoun.service;

import com.example.ngoun.dto.ShopOrderRequest;
import com.example.ngoun.dto.ShopOrderStatusUpdateRequest;
import com.example.ngoun.model.ShopOrder;
import com.example.ngoun.model.ShopOrderItem;
import com.example.ngoun.repository.ShopOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShopOrderService {

    private static final String ID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int ID_LENGTH = 6;
    private static final int MAX_ID_ATTEMPTS = 10;
    private final SecureRandom random = new SecureRandom();

    private final ShopOrderRepository repository;

    public List<ShopOrder> findAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<ShopOrder> findById(String id) {
        return repository.findById(id);
    }

    @Transactional
    public ShopOrder create(ShopOrderRequest req) {
        ShopOrder o = new ShopOrder();
        o.setId(generateUniqueId());
        o.setClientName(req.getClientName());
        o.setClientPhone(req.getClientPhone());
        o.setClientEmail(req.getClientEmail());
        o.setAddress(req.getAddress());
        o.setPaymentMethod(ShopOrder.PaymentMethod.valueOf(req.getPaymentMethod().toUpperCase()));
        if (req.getPaymentStatus() != null) {
            o.setPaymentStatus(ShopOrder.PaymentStatus.valueOf(req.getPaymentStatus().toUpperCase()));
        }
        o.setPaymentId(req.getPaymentId());

        long total = 0;
        if (req.getItems() != null) {
            for (ShopOrderRequest.ItemRef ref : req.getItems()) {
                ShopOrderItem item = new ShopOrderItem();
                item.setOrder(o);
                item.setProductId(ref.getProductId());
                item.setProductName(ref.getProductName());
                item.setQty(ref.getQty());
                item.setPrice(ref.getPrice());
                o.getItems().add(item);
                total += (long) ref.getQty() * ref.getPrice();
            }
        }
        o.setTotal(total);

        return repository.save(o);
    }

    @Transactional
    public Optional<ShopOrder> updateStatus(String id, ShopOrderStatusUpdateRequest req) {
        return repository.findById(id).map(o -> {
            if (req.getStatus() != null) {
                o.setStatus(ShopOrder.Status.valueOf(req.getStatus().toUpperCase()));
            }
            if (req.getPaymentStatus() != null) {
                o.setPaymentStatus(ShopOrder.PaymentStatus.valueOf(req.getPaymentStatus().toUpperCase()));
            }
            if (req.getPaymentId() != null) {
                o.setPaymentId(req.getPaymentId());
            }
            return repository.save(o);
        });
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    private String generateUniqueId() {
        for (int i = 0; i < MAX_ID_ATTEMPTS; i++) {
            String candidate = "ORD-" + randomSuffix();
            if (!repository.existsById(candidate)) return candidate;
        }
        throw new IllegalStateException("Could not generate a unique order id after " + MAX_ID_ATTEMPTS + " attempts");
    }

    private String randomSuffix() {
        StringBuilder sb = new StringBuilder(ID_LENGTH);
        for (int i = 0; i < ID_LENGTH; i++) sb.append(ID_CHARS.charAt(random.nextInt(ID_CHARS.length())));
        return sb.toString();
    }
}
