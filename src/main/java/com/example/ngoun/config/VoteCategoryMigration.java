package com.example.ngoun.config;

import com.example.ngoun.model.VoteCategory;
import com.example.ngoun.model.VoteProfile;
import com.example.ngoun.repository.VoteProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Brings the database to "one vote per category" (Miss / Mister). Idempotent:
 * every step only touches what is not yet in the target state.
 *  1. classify profiles without a category (from their presentation text);
 *  2. copy each vote's category from its profile;
 *  3. replace the old one-vote-per-email rule with one per (email, category);
 *  4. replace one-verified-vote-per-device with one per (device, category).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VoteCategoryMigration {

    private final JdbcTemplate jdbc;
    private final VoteProfileRepository profiles;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void migrate() {
        try {
            List<VoteProfile> unclassified = profiles.findAll().stream().filter(p -> p.getCategory() == null).toList();
            unclassified.forEach(p -> p.setCategory(VoteCategory.guessFrom(p.getDescription())));
            profiles.saveAll(unclassified);
            if (!unclassified.isEmpty()) log.info("Vote categories: classified {} profile(s)", unclassified.size());

            int votes = jdbc.update("update votes v set category = p.category from vote_profiles p "
                    + "where v.vote_profile_id = p.id and (v.category is null or v.category <> p.category)");
            if (votes > 0) log.info("Vote categories: set category on {} vote(s)", votes);

            List<String> emailOnly = jdbc.queryForList(
                    "select c.conname from pg_constraint c join pg_attribute a "
                            + "on a.attrelid = c.conrelid and a.attnum = c.conkey[1] "
                            + "where c.conrelid = 'votes'::regclass and c.contype = 'u' "
                            + "and array_length(c.conkey, 1) = 1 and a.attname = 'email'", String.class);
            for (String name : emailOnly) {
                jdbc.execute("alter table votes drop constraint \"" + name.replace("\"", "") + "\"");
                log.info("Vote categories: dropped one-vote-per-email constraint {}", name);
            }
            jdbc.execute("create unique index if not exists votes_one_per_email_category on votes (email, category)");

            jdbc.execute("drop index if exists votes_one_verified_per_device");
            jdbc.execute("create unique index if not exists votes_one_verified_per_device_category "
                    + "on votes (device_id, category) where verified and device_id is not null");
        } catch (Exception e) {
            log.error("Vote category migration failed", e);
        }
    }
}
