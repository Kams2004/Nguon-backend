package com.example.ngoun.votestats;

import com.example.ngoun.model.VoteProfile;
import com.example.ngoun.repository.VoteProfileRepository;
import com.example.ngoun.service.PresignedUrlCache;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Vote analytics for the admin: confirmed votes per candidate, ranking, share
 * and day-by-day evolution. Counts come from verified rows in `votes`; times
 * are stored in UTC and reported in the festival's time zone. No voter email
 * ever leaves this service.
 */
@Service
@RequiredArgsConstructor
public class VoteStatsService {

    private final JdbcTemplate jdbc;
    private final VoteProfileRepository profiles;
    private final PresignedUrlCache urlCache;

    @Value("${app.report-timezone:Africa/Douala}")
    private String reportZone;

    public record DayCount(LocalDate date, long votes) {}

    public record CandidateStats(Long id, String name, String description, String photoUrl, String photoPresignedUrl,
                                 boolean published, long votes, int rank, double share,
                                 LocalDateTime firstVoteAt, LocalDateTime lastVoteAt, long votesLast24h,
                                 List<DayCount> daily) {}

    public record Overview(LocalDateTime generatedAt, String timezone, long totalVotes, long codesRequested,
                           long pendingCodes, long devices, int candidates,
                           LocalDateTime firstVoteAt, LocalDateTime lastVoteAt,
                           List<DayCount> daily, List<CandidateStats> ranking) {}

    private record Row(long profileId, LocalDateTime verifiedAt) {}

    public Overview overview() {
        ZoneId zone = ZoneId.of(reportZone);
        List<Row> rows = verifiedRows(zone);
        Map<Long, List<Row>> byProfile = new HashMap<>();
        rows.forEach(r -> byProfile.computeIfAbsent(r.profileId(), k -> new ArrayList<>()).add(r));

        long total = rows.size();
        List<VoteProfile> all = profiles.findAllByOrderByCreatedAtAsc();
        List<CandidateStats> ranking = new ArrayList<>();
        for (VoteProfile p : all) ranking.add(build(p, byProfile.getOrDefault(p.getId(), List.of()), total, zone, 0));
        ranking.sort(Comparator.comparingLong(CandidateStats::votes).reversed().thenComparing(CandidateStats::name));

        // Competition ranking: equal votes share a rank (1, 1, 3, ...).
        List<CandidateStats> ranked = new ArrayList<>();
        for (int i = 0; i < ranking.size(); i++) {
            CandidateStats c = ranking.get(i);
            int rank = (i > 0 && ranking.get(i - 1).votes() == c.votes()) ? ranked.get(i - 1).rank() : i + 1;
            ranked.add(withRank(c, rank));
        }

        Map<String, Object> counts = jdbc.queryForMap(
                "select count(*) as requested, count(*) filter (where not verified) as pending, "
                        + "count(distinct device_id) filter (where verified and device_id is not null) as devices from votes");

        return new Overview(LocalDateTime.now(zone), reportZone, total,
                ((Number) counts.get("requested")).longValue(), ((Number) counts.get("pending")).longValue(),
                ((Number) counts.get("devices")).longValue(), all.size(),
                rows.isEmpty() ? null : rows.get(0).verifiedAt(),
                rows.isEmpty() ? null : rows.get(rows.size() - 1).verifiedAt(),
                daily(rows), ranked);
    }

    public Optional<CandidateStats> candidate(Long id) {
        return overview().ranking().stream().filter(c -> c.id().equals(id)).findFirst();
    }

    private CandidateStats build(VoteProfile p, List<Row> rows, long total, ZoneId zone, int rank) {
        LocalDateTime dayAgo = LocalDateTime.now(zone).minusHours(24);
        return new CandidateStats(p.getId(), p.getName(), p.getDescription(), p.getPhotoUrl(),
                urlCache.get(p.getPhotoUrl()), p.isPublished(), rows.size(), rank,
                total == 0 ? 0 : rows.size() * 100.0 / total,
                rows.isEmpty() ? null : rows.get(0).verifiedAt(),
                rows.isEmpty() ? null : rows.get(rows.size() - 1).verifiedAt(),
                rows.stream().filter(r -> r.verifiedAt().isAfter(dayAgo)).count(),
                daily(rows));
    }

    private static CandidateStats withRank(CandidateStats c, int rank) {
        return new CandidateStats(c.id(), c.name(), c.description(), c.photoUrl(), c.photoPresignedUrl(), c.published(),
                c.votes(), rank, c.share(), c.firstVoteAt(), c.lastVoteAt(), c.votesLast24h(), c.daily());
    }

    private List<Row> verifiedRows(ZoneId zone) {
        return jdbc.query(
                "select vote_profile_id, verified_at from votes where verified and verified_at is not null order by verified_at",
                (rs, i) -> {
                    Timestamp ts = rs.getTimestamp("verified_at");
                    LocalDateTime local = ts.toLocalDateTime().atZone(ZoneOffset.UTC).withZoneSameInstant(zone).toLocalDateTime();
                    return new Row(rs.getLong("vote_profile_id"), local);
                });
    }

    private static List<DayCount> daily(List<Row> rows) {
        TreeMap<LocalDate, Long> days = new TreeMap<>();
        rows.forEach(r -> days.merge(r.verifiedAt().toLocalDate(), 1L, Long::sum));
        if (days.isEmpty()) return List.of();
        List<DayCount> out = new ArrayList<>();
        for (LocalDate d = days.firstKey(); !d.isAfter(days.lastKey()); d = d.plusDays(1))
            out.add(new DayCount(d, days.getOrDefault(d, 0L)));
        return out;
    }
}
