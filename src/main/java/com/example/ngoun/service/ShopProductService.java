package com.example.ngoun.service;

import com.example.ngoun.dto.ShopProductRequest;
import com.example.ngoun.model.ShopProduct;
import com.example.ngoun.model.ShopProductMedia;
import com.example.ngoun.repository.ShopProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopProductService {

    private final ShopProductRepository repository;
    private final PresignedUrlCache urlCache;

    public List<ShopProduct> findAll() {
        return repository.findAllByOrderByFeaturedDescCreatedAtDesc()
                .stream().map(this::enrich).toList();
    }

    public Optional<ShopProduct> findById(Long id) {
        return repository.findById(id).map(this::enrich);
    }

    @Transactional
    public ShopProduct create(ShopProductRequest req) {
        ShopProduct p = new ShopProduct();
        applyFields(p, req);
        addNewMedia(p, req);
        return enrich(repository.save(p));
    }

    @Transactional
    public Optional<ShopProduct> update(Long id, ShopProductRequest req) {
        return repository.findById(id).map(p -> {
            p.getMedia().forEach(m -> urlCache.invalidate(m.getUrl()));
            applyFields(p, req);
            reconcileMedia(p, req);
            return enrich(repository.save(p));
        });
    }

    public void delete(Long id) {
        repository.findById(id).ifPresent(p -> {
            p.getMedia().forEach(m -> urlCache.invalidate(m.getUrl()));
            repository.delete(p);
        });
    }

    // --- Helpers ---

    private void applyFields(ShopProduct p, ShopProductRequest req) {
        p.setCategory(req.getCategory());
        p.setName(req.getName());
        p.setTagline(req.getTagline());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setComparePrice(req.getComparePrice());
        p.setUnit(req.getUnit());
        p.setInStock(req.isInStock());
        p.setStockQty(req.getStockQty());
        p.setSeller(req.getSeller());
        p.setSellerLocation(req.getSellerLocation());
        p.setPhone(req.getPhone());
        p.setWhatsapp(req.getWhatsapp());
        p.setTags(req.getTags());
        p.setFeatured(req.isFeatured());
        p.setBadge(req.getBadge());
    }

    private void addNewMedia(ShopProduct p, ShopProductRequest req) {
        if (req.getMedia() == null) return;
        req.getMedia().stream()
                .filter(m -> m.getId() == null)
                .forEach(m -> p.getMedia().add(buildMedia(p, m.getType(), m.getUrl(), m.getAlt(), m.getDisplayOrder())));
    }

    private void reconcileMedia(ShopProduct p, ShopProductRequest req) {
        if (req.getMedia() == null) return;

        Set<Long> keepIds = req.getMedia().stream()
                .filter(m -> m.getId() != null)
                .map(ShopProductRequest.MediaRef::getId)
                .collect(Collectors.toSet());

        // Remove absent existing media
        p.getMedia().removeIf(m -> !keepIds.contains(m.getId()));

        // Add new media (no id)
        req.getMedia().stream()
                .filter(m -> m.getId() == null)
                .forEach(m -> p.getMedia().add(buildMedia(p, m.getType(), m.getUrl(), m.getAlt(), m.getDisplayOrder())));
    }

    private ShopProductMedia buildMedia(ShopProduct p, String type, String url, String alt, Integer order) {
        ShopProductMedia m = new ShopProductMedia();
        m.setProduct(p);
        m.setType(ShopProductMedia.MediaType.valueOf(type.toUpperCase()));
        m.setUrl(url);
        m.setAlt(alt);
        m.setDisplayOrder(order);
        return m;
    }

    private ShopProduct enrich(ShopProduct p) {
        p.getMedia().forEach(this::enrichMedia);
        return p;
    }

    private ShopProductMedia enrichMedia(ShopProductMedia m) {
        m.setPresignedUrl(urlCache.get(m.getUrl()));
        return m;
    }
}
