package com.example.ngoun.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Database-level guarantee of one verified vote per device, even if two
 * confirmations from the same phone race each other. Hibernate's ddl-auto
 * cannot express a partial unique index, so it is created here (idempotent).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceVoteIndex {

    private final JdbcTemplate jdbc;

    @EventListener(ApplicationReadyEvent.class)
    public void ensureIndex() {
        try {
            jdbc.execute("CREATE UNIQUE INDEX IF NOT EXISTS votes_one_verified_per_device "
                    + "ON votes (device_id) WHERE verified AND device_id IS NOT NULL");
        } catch (Exception e) {
            log.error("Could not create the one-vote-per-device index", e);
        }
    }
}
