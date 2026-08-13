package com.example.ngoun.service;

import com.example.ngoun.dto.BookingMediaRequest;
import com.example.ngoun.dto.BookingPropertyRequest;
import com.example.ngoun.model.BookingMedia;
import com.example.ngoun.model.BookingProperty;
import com.example.ngoun.repository.BookingMediaRepository;
import com.example.ngoun.repository.BookingPropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingPropertyService {

    private final BookingPropertyRepository propertyRepo;
    private final BookingMediaRepository mediaRepo;
    private final PresignedUrlCache urlCache;

    public List<BookingProperty> findPublished() {
        return propertyRepo.findByPublishedTrueOrderByFeaturedDescCreatedAtDesc()
                .stream().map(this::enrich).toList();
    }

    public List<BookingProperty> findAll() {
        return propertyRepo.findAllByOrderByFeaturedDescCreatedAtDesc()
                .stream().map(this::enrich).toList();
    }

    public Optional<BookingProperty> findById(Long id) {
        return propertyRepo.findById(id).map(this::enrich);
    }

    @Transactional
    public BookingProperty create(BookingPropertyRequest req) {
        BookingProperty p = new BookingProperty();
        applyFields(p, req);
        BookingProperty saved = propertyRepo.save(p);
        addNewMedia(saved, req);
        return enrich(propertyRepo.save(saved));
    }

    @Transactional
    public Optional<BookingProperty> update(Long id, BookingPropertyRequest req) {
        return propertyRepo.findById(id).map(p -> {
            p.getMedia().forEach(m -> urlCache.invalidate(m.getUrl()));
            applyFields(p, req);
            reconcileMedia(p, req);
            return enrich(propertyRepo.save(p));
        });
    }

    public void delete(Long id) {
        propertyRepo.findById(id).ifPresent(p -> {
            p.getMedia().forEach(m -> urlCache.invalidate(m.getUrl()));
            propertyRepo.delete(p);
        });
    }

    // --- Media sub-resource ---

    public List<BookingMedia> getMedia(Long propertyId) {
        return mediaRepo.findByPropertyId(propertyId).stream().map(this::enrichMedia).toList();
    }

    @Transactional
    public Optional<BookingMedia> addMedia(Long propertyId, BookingMediaRequest req) {
        return propertyRepo.findById(propertyId).map(p -> {
            BookingMedia m = buildMedia(p, req.getType(), req.getUrl(), req.getAlt(), req.getDisplayOrder());
            return enrichMedia(mediaRepo.save(m));
        });
    }

    public void deleteMedia(Long mediaId) {
        mediaRepo.findById(mediaId).ifPresent(m -> {
            urlCache.invalidate(m.getUrl());
            mediaRepo.delete(m);
        });
    }

    // --- Helpers ---

    private void applyFields(BookingProperty p, BookingPropertyRequest req) {
        p.setCategory(BookingProperty.Category.valueOf(req.getCategory().toUpperCase()));
        p.setName(req.getName());
        p.setTagline(req.getTagline());
        p.setDescription(req.getDescription());
        p.setAddress(req.getAddress());
        p.setPhone(req.getPhone());
        p.setWhatsapp(req.getWhatsapp());
        p.setEmail(req.getEmail());
        p.setWebsite(req.getWebsite());
        p.setPriceFrom(req.getPriceFrom());
        p.setPriceTo(req.getPriceTo());
        p.setPriceUnit(req.getPriceUnit());
        p.setStars(req.getStars());
        p.setCuisine(req.getCuisine());
        p.setOpeningHours(req.getOpeningHours());
        p.setFeatures(req.getFeatures());
        p.setAccentColor(req.getAccentColor());
        p.setFeatured(req.isFeatured());
        p.setPublished(req.isPublished());
    }

    private void addNewMedia(BookingProperty p, BookingPropertyRequest req) {
        if (req.getMedia() == null) return;
        req.getMedia().stream()
                .filter(m -> m.getId() == null)
                .forEach(m -> mediaRepo.save(buildMedia(p, m.getType(), m.getUrl(), m.getAlt(), m.getDisplayOrder())));
    }

    private void reconcileMedia(BookingProperty p, BookingPropertyRequest req) {
        if (req.getMedia() == null) return;

        Set<Long> keepIds = req.getMedia().stream()
                .filter(m -> m.getId() != null)
                .map(BookingPropertyRequest.MediaRef::getId)
                .collect(Collectors.toSet());

        // Remove absent existing media
        p.getMedia().removeIf(m -> !keepIds.contains(m.getId()));

        // Add new media (no id)
        req.getMedia().stream()
                .filter(m -> m.getId() == null)
                .forEach(m -> p.getMedia().add(buildMedia(p, m.getType(), m.getUrl(), m.getAlt(), m.getDisplayOrder())));
    }

    private BookingMedia buildMedia(BookingProperty p, String type, String url, String alt, Integer order) {
        BookingMedia m = new BookingMedia();
        m.setProperty(p);
        m.setType(BookingMedia.MediaType.valueOf(type.toUpperCase()));
        m.setUrl(url);
        m.setAlt(alt);
        m.setDisplayOrder(order);
        return m;
    }

    private BookingProperty enrich(BookingProperty p) {
        p.getMedia().forEach(this::enrichMedia);
        return p;
    }

    private BookingMedia enrichMedia(BookingMedia m) {
        m.setPresignedUrl(urlCache.get(m.getUrl()));
        return m;
    }
}
