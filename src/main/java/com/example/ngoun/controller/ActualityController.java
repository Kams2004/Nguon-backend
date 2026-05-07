package com.example.ngoun.controller;

import com.example.ngoun.model.Actuality;
import com.example.ngoun.service.ActualityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/actualities")
@RequiredArgsConstructor
public class ActualityController {
    private final ActualityService actualityService;
    
    @GetMapping
    public ResponseEntity<List<Actuality>> getAllActualities() {
        return ResponseEntity.ok(actualityService.getAllActualities());
    }
    
    @GetMapping("/published")
    public ResponseEntity<List<Actuality>> getPublishedActualities() {
        return ResponseEntity.ok(actualityService.getPublishedActualities());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Actuality> getActualityById(@PathVariable Long id) {
        Actuality actuality = actualityService.getActualityById(id);
        return actuality != null ? ResponseEntity.ok(actuality) : ResponseEntity.notFound().build();
    }
    
    @PostMapping
    public ResponseEntity<Actuality> createActuality(@RequestBody Actuality actuality) {
        return ResponseEntity.ok(actualityService.createActuality(actuality));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Actuality> updateActuality(@PathVariable Long id, @RequestBody Actuality actuality) {
        Actuality updated = actualityService.updateActuality(id, actuality);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActuality(@PathVariable Long id) {
        actualityService.deleteActuality(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/preview/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> previewActuality(@PathVariable Long id) {
        Actuality actuality = actualityService.getActualityById(id);
        if (actuality == null || !Boolean.TRUE.equals(actuality.getPublished())) {
            return ResponseEntity.notFound().build();
        }

        String spaUrl = "https://www.nguonevents.com/?news=" + id;
        String encodedMedia = URLEncoder.encode(actuality.getMedia(), StandardCharsets.UTF_8).replace("+", "%20");
        String imageUrl = "https://www.nguonevents.com/api/files/view/" + encodedMedia;
        String title = escapeHtml(actuality.getTitle());
        String description = actuality.getDescription() != null
                ? escapeHtml(actuality.getDescription().length() > 200
                    ? actuality.getDescription().substring(0, 200) + "..."
                    : actuality.getDescription())
                : "";

        String html = "<!DOCTYPE html>\n"
                + "<html lang=\"fr\">\n"
                + "<head>\n"
                + "<meta charset=\"UTF-8\">\n"
                + "<meta http-equiv=\"refresh\" content=\"0;url=" + spaUrl + "\">\n"
                + "<title>" + title + "</title>\n"
                // Open Graph
                + "<meta property=\"og:type\" content=\"article\">\n"
                + "<meta property=\"og:url\" content=\"" + spaUrl + "\">\n"
                + "<meta property=\"og:title\" content=\"" + title + "\">\n"
                + "<meta property=\"og:description\" content=\"" + description + "\">\n"
                + "<meta property=\"og:image\" content=\"" + imageUrl + "\">\n"
                + "<meta property=\"og:image:width\" content=\"1200\">\n"
                + "<meta property=\"og:image:height\" content=\"630\">\n"
                + "<meta property=\"og:site_name\" content=\"Nguon Events\">\n"
                // Twitter Card
                + "<meta name=\"twitter:card\" content=\"summary_large_image\">\n"
                + "<meta name=\"twitter:title\" content=\"" + title + "\">\n"
                + "<meta name=\"twitter:description\" content=\"" + description + "\">\n"
                + "<meta name=\"twitter:image\" content=\"" + imageUrl + "\">\n"
                + "</head>\n"
                + "<body>\n"
                + "<script>window.location.replace(\"" + spaUrl + "\");</script>\n"
                + "</body>\n"
                + "</html>";

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
