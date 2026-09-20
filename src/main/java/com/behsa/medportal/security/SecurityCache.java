package com.behsa.medportal.security;

import com.behsa.medportal.security.jwt.SessionInfo;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory, single-JVM cache of active server-side sessions and of the per-login rate limiters.
 * <p>
 * Both maps are bounded: sessions are evicted by a scheduled sweep once their inactivity window has
 * passed, and the login rate-limit buckets have both a maximum size and an idle TTL so that an
 * attacker who varies the login key on every request cannot grow the heap without bound.
 * <p>
 * Note: this is deliberately in-JVM. It is not shared across a cluster; that limitation is tracked
 * separately.
 */
@Component
public class SecurityCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityCache.class);

    /** Default server-side inactivity window (minutes) when {@code user-session.expire.per-minute} is unset. */
    public static final long DEFAULT_INACTIVITY_MINUTES = 30L;

    /**
     * Fallback remember-me server-side inactivity window (minutes) when
     * {@code user-session.expire.remember-me-per-minute} is unset: 30 days.
     * <p>
     * This must stay aligned with
     * {@code jhipster.security.authentication.jwt.token-validity-in-seconds-for-remember-me}
     * (2592000 s = 30 days). If the server-side window is shorter than the JWT lifetime the user is
     * silently logged out while still holding a valid token.
     */
    public static final long DEFAULT_REMEMBER_ME_INACTIVITY_MINUTES = 30L * 24L * 60L;

    /** Hard cap on the number of distinct login rate-limit buckets kept in memory. */
    static final int MAX_LOGIN_BUCKETS = 10_000;

    /**
     * Minimum idle TTL (minutes) for a login bucket. The effective TTL is the larger of this value
     * and the time a fully drained bucket needs to refill, so eviction can never be used to reset an
     * in-flight rate limit.
     */
    static final long LOGIN_BUCKET_MIN_TTL_MINUTES = 10L;

    /** Fraction of the login bucket map dropped (oldest first) when the size cap is hit. */
    private static final double LOGIN_BUCKET_OVERFLOW_EVICTION_RATIO = 0.10d;

    /** Sweep interval (ms) for the expired-session and stale-login-bucket eviction. */
    private static final long CLEANUP_FIXED_DELAY_MS = 60_000L;

    @Value("${bucket4j.get.bucket-size}")
    long bucketSizeForGet;

    @Value("${bucket4j.get.token-per-minute}")
    long tokenPerMinuteForGet;

    @Value("${bucket4j.post.bucket-size}")
    long bucketSizeForPost;

    @Value("${bucket4j.post.token-per-minute}")
    long tokenPerMinuteForPost;

    // Defaults match the previously hard-coded login bucket (capacity 10, refill 10/minute).
    @Value("${bucket4j.login.bucket-size:10}")
    long bucketSizeForLogin;

    @Value("${bucket4j.login.token-per-minute:10}")
    long tokenPerMinuteForLogin;

    @Value("${user-session.expire.per-minute:30}")
    long sessionExpirePerMinute;

    /**
     * Server-side inactivity window for remember-me sessions, in minutes. Defaults to 30 days to
     * match the remember-me JWT validity; see {@link #DEFAULT_REMEMBER_ME_INACTIVITY_MINUTES}.
     */
    @Value("${user-session.expire.remember-me-per-minute:43200}")
    long rememberMeSessionExpirePerMinute;

    /** Keyed by the SHA-256 hash of the bearer token, never by the raw token itself. */
    private final Map<String, SessionInfo> sessionInfos = new ConcurrentHashMap<>();
    private final Map<String, LoginBucketEntry> loginBuckets = new ConcurrentHashMap<>();

    public SecurityCache() {

    }

    public void storeSession(
        Object principal,
        String sessionId,
        String ip,
        String username,
        String token,
        String userAgent,
        LocalDateTime login,
        LocalDateTime logout,
        Boolean validToken
    ) {
        storeSession(
            principal,
            sessionId,
            ip,
            username,
            token,
            userAgent,
            login,
            logout,
            validToken,
            resolveDefaultInactivityMinutes()
        );
    }

    public void storeSession(
        Object principal,
        String sessionId,
        String ip,
        String username,
        String token,
        String userAgent,
        LocalDateTime login,
        LocalDateTime logout,
        Boolean validToken,
        long inactivityMinutes
    ) {
        String tokenHash = hashToken(token);
        if (tokenHash == null) {
            LOGGER.warn("Refusing to store a session without a token");
            return;
        }

        long minutes = inactivityMinutes > 0 ? inactivityMinutes : resolveDefaultInactivityMinutes();
        sessionInfos.put(
            tokenHash,
            new SessionInfo(
                principal,
                sessionId,
                ip,
                username,
                // Only the hash is retained: a heap dump must not hand over live bearer tokens.
                tokenHash,
                userAgent,
                login,
                logout,
                validToken,
                login,
                createNewBucket("get"),
                createNewBucket("post"),
                minutes
            )
        );
    }

    private long resolveDefaultInactivityMinutes() {
        return sessionExpirePerMinute > 0 ? sessionExpirePerMinute : DEFAULT_INACTIVITY_MINUTES;
    }

    /** Configured inactivity window (minutes) for regular, non-remember-me sessions. */
    public long getDefaultInactivityMinutes() {
        return resolveDefaultInactivityMinutes();
    }

    /**
     * Configured inactivity window (minutes) for remember-me sessions. Single source of truth for the
     * server-side remember-me window; keep it aligned with the remember-me JWT validity.
     */
    public long getRememberMeInactivityMinutes() {
        return rememberMeSessionExpirePerMinute > 0
            ? rememberMeSessionExpirePerMinute
            : DEFAULT_REMEMBER_ME_INACTIVITY_MINUTES;
    }

    public void removeSession(String jwtToken) {
        String tokenHash = hashToken(jwtToken);
        if (tokenHash == null) {
            return;
        }

        sessionInfos.remove(tokenHash);
    }

    public List<SessionInfo> getAllSessionInfo() {
        try {
            sessionInfos.entrySet().removeIf(entry -> entry.getValue() == null || isTokenExpired(entry.getValue()));
            return new ArrayList<>(sessionInfos.values());
        } catch (Exception ex) {
            LOGGER.error("Error occurred in getAllSessionInfo",ex);
            return null;
        }
    }


    public void removeSessionsByUsername(String username) {
        try {
            if (username == null || username.isBlank() || sessionInfos.isEmpty()) {
                return;
            }

            sessionInfos.entrySet().removeIf(entry -> {
                SessionInfo sessionInfo = entry.getValue();

                return sessionInfo != null
                    && sessionInfo.getUsername() != null
                    && sessionInfo.getUsername().equalsIgnoreCase(username);
            });
        } catch (Exception ex) {
            LOGGER.error("Error occurred in removeSessionsByUsername", ex);
        }
    }

    public SessionInfo getSessionInfoByToken(String jwtToken) {
        try {
            String tokenHash = hashToken(jwtToken);
            if (tokenHash == null || sessionInfos.isEmpty()) return null;

            SessionInfo sessionInfo = sessionInfos.get(tokenHash);
            if (sessionInfo != null) {
                if (isTokenExpired(sessionInfo)) sessionInfos.remove(tokenHash);
                else return sessionInfo.setLastActionDate(LocalDateTime.now());
            }

            return null;
        } catch (Exception ex) {
            LOGGER.error("Error occurred in getSessionInfoByToken",ex);
            return null;
        }
    }

    public Boolean hasConcurrentSession(String username) {
        try {
            if (username == null) return false;
            Map<String, SessionInfo> result = sessionInfos
                .entrySet()
                .stream()
                .filter(map -> username.equalsIgnoreCase(map.getValue().getUsername()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            return !result.isEmpty();
        } catch (Exception ex) {
            LOGGER.error("Error occurred in hasConcurrentSession",ex);
            return false;
        }
    }

    public SessionInfo fetchSessionInfo(String username) {
        try {
            if (username == null) return null;
            Map<String, SessionInfo> result = sessionInfos
                .entrySet()
                .stream()
                .filter(map -> username.equalsIgnoreCase(map.getValue().getUsername()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            return result.values().stream().findFirst().orElse(null);
        } catch (Exception ex) {
            LOGGER.error("Error occurred in fetchSessionInfo",ex);
            return null;
        }
    }

    /**
     * Periodically drops sessions whose inactivity window has passed, so abandoned sessions do not
     * sit in the map until something happens to touch them.
     */
    @Scheduled(fixedDelay = CLEANUP_FIXED_DELAY_MS)
    public void cleanupExpiredSessions() {
        try {
            int before = sessionInfos.size();
            sessionInfos.entrySet().removeIf(entry -> entry.getValue() == null || isTokenExpired(entry.getValue()));
            int removed = before - sessionInfos.size();

            if (removed > 0) {
                LOGGER.debug("Evicted {} expired session(s), {} session(s) remaining", removed, sessionInfos.size());
            }
        } catch (Exception ex) {
            LOGGER.error("Error occurred in cleanupExpiredSessions", ex);
        }
    }

    /**
     * Periodically drops login rate-limit buckets that have been idle longer than the effective TTL.
     * The TTL is never shorter than the bucket's refill window, so an evicted bucket was already
     * fully refilled and eviction cannot be used to reset a live rate limit.
     */
    @Scheduled(fixedDelay = CLEANUP_FIXED_DELAY_MS)
    public void cleanupStaleLoginBuckets() {
        try {
            int removed = evictIdleLoginBuckets(System.currentTimeMillis());

            if (removed > 0) {
                LOGGER.debug("Evicted {} idle login bucket(s), {} bucket(s) remaining", removed, loginBuckets.size());
            }
        } catch (Exception ex) {
            LOGGER.error("Error occurred in cleanupStaleLoginBuckets", ex);
        }
    }

    private boolean isTokenExpired(SessionInfo sessionInfo) {
        long inactivityLimit = sessionInfo.getInactivityMinutes() > 0
            ? sessionInfo.getInactivityMinutes()
            : resolveDefaultInactivityMinutes();
        return sessionInfo.getLastActionDate().isBefore(LocalDateTime.now().minusMinutes(inactivityLimit));
    }

    private Bucket createNewBucket(String reqType) {
        Bandwidth limit;

        if (reqType.equals("get")) {
            limit = Bandwidth.builder()
                .capacity(bucketSizeForGet)
                .refillGreedy(tokenPerMinuteForGet, Duration.ofMinutes(1))
                .build();
        } else {
            limit = Bandwidth.builder()
                .capacity(bucketSizeForPost)
                .refillGreedy(tokenPerMinuteForPost, Duration.ofMinutes(1))
                .build();
        }

        return Bucket.builder().addLimit(limit).build();
    }

    public boolean tryConsumeLogin(String key) {
        String bucketKey = (key == null || key.isBlank()) ? "unknown" : key;
        long now = System.currentTimeMillis();

        LoginBucketEntry entry = loginBuckets.get(bucketKey);

        if (entry == null) {
            // Bound the map before admitting a new key, otherwise varying the login key on every
            // request grows the map without limit.
            if (loginBuckets.size() >= MAX_LOGIN_BUCKETS) {
                evictIdleLoginBuckets(now);
            }
            if (loginBuckets.size() >= MAX_LOGIN_BUCKETS) {
                evictOldestLoginBuckets();
            }

            entry = loginBuckets.computeIfAbsent(bucketKey, ignored -> new LoginBucketEntry(createLoginBucket(), now));
        }

        entry.lastAccessMillis = now;

        return entry.bucket.tryConsume(1);
    }

    private Bucket createLoginBucket() {
        long capacity = bucketSizeForLogin > 0 ? bucketSizeForLogin : 10L;
        long refill = tokenPerMinuteForLogin > 0 ? tokenPerMinuteForLogin : 10L;

        return Bucket
            .builder()
            .addLimit(
                Bandwidth.builder()
                    .capacity(capacity)
                    .refillGreedy(refill, Duration.ofMinutes(1))
                    .build()
            )
            .build();
    }

    /**
     * Effective idle TTL for a login bucket, in milliseconds. Never shorter than the time a fully
     * drained bucket needs to refill to capacity, so evicting an idle bucket cannot hand an attacker
     * a free reset of the login rate limit.
     */
    long loginBucketTtlMillis() {
        long capacity = bucketSizeForLogin > 0 ? bucketSizeForLogin : 10L;
        long refill = tokenPerMinuteForLogin > 0 ? tokenPerMinuteForLogin : 10L;
        long refillWindowMinutes = (long) Math.ceil((double) capacity / (double) refill);
        long ttlMinutes = Math.max(LOGIN_BUCKET_MIN_TTL_MINUTES, refillWindowMinutes);

        return Duration.ofMinutes(ttlMinutes).toMillis();
    }

    private int evictIdleLoginBuckets(long now) {
        long ttlMillis = loginBucketTtlMillis();
        int before = loginBuckets.size();
        loginBuckets.entrySet().removeIf(entry -> entry.getValue() == null || now - entry.getValue().lastAccessMillis >= ttlMillis);

        return before - loginBuckets.size();
    }

    /**
     * Last-resort bound: when the cap is reached and nothing is idle enough to expire, drop the least
     * recently used slice of the map so new logins are still admitted.
     */
    private void evictOldestLoginBuckets() {
        int target = Math.max(1, (int) (MAX_LOGIN_BUCKETS * LOGIN_BUCKET_OVERFLOW_EVICTION_RATIO));

        Comparator<Map.Entry<String, LoginBucketEntry>> byLastAccess = Comparator.comparingLong(
            entry -> entry.getValue().lastAccessMillis
        );

        List<String> oldest = loginBuckets
            .entrySet()
            .stream()
            .sorted(byLastAccess)
            .limit(target)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        oldest.forEach(loginBuckets::remove);

        LOGGER.debug("Login bucket cache reached its {} entry cap, evicted {} least recently used entries",
            MAX_LOGIN_BUCKETS, oldest.size());
    }

    /** Visible for testing. */
    int loginBucketCount() {
        return loginBuckets.size();
    }

    /** Visible for testing. */
    int sessionCount() {
        return sessionInfos.size();
    }

    /**
     * SHA-256 of the bearer token, hex encoded. Used as the session cache key so neither the map keys
     * nor {@link SessionInfo} ever hold a usable bearer token.
     */
    public static String hashToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            // SHA-256 is mandated by the JDK; this cannot happen on a compliant runtime.
            throw new IllegalStateException("SHA-256 is not available, cannot key the session cache", ex);
        }
    }

    private static final class LoginBucketEntry {

        private final Bucket bucket;
        private volatile long lastAccessMillis;

        private LoginBucketEntry(Bucket bucket, long lastAccessMillis) {
            this.bucket = bucket;
            this.lastAccessMillis = lastAccessMillis;
        }
    }
}
