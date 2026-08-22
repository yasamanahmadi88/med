package com.behsa.medportal.security;

import com.behsa.medportal.security.jwt.SessionInfo;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class SecurityCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityCache.class);

    /** Default server-side inactivity window (minutes). */
    public static final long DEFAULT_INACTIVITY_MINUTES = 30L;
    /** Remember-me server-side inactivity window: 7 days. */
    public static final long REMEMBER_ME_INACTIVITY_MINUTES = 7L * 24L * 60L;

    @Value("${bucket4j.get.bucket-size}")
    long bucketSizeForGet;

    @Value("${bucket4j.get.token-per-minute}")
    long tokenPerMinuteForGet;

    @Value("${bucket4j.post.bucket-size}")
    long bucketSizeForPost;

    @Value("${bucket4j.post.token-per-minute}")
    long tokenPerMinuteForPost;

    @Value("${user-session.expire.per-minute:30}")
    long sessionExpirePerMinute;


    private final Map<String, SessionInfo> sessionInfos = new ConcurrentHashMap<>();
    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();

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
        long minutes = inactivityMinutes > 0 ? inactivityMinutes : resolveDefaultInactivityMinutes();
        sessionInfos.put(
            token,
            new SessionInfo(
                principal,
                sessionId,
                ip,
                username,
                token,
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



    public void removeSession(String jwtToken) {
        if (jwtToken == null || jwtToken.isBlank()) {
            return;
        }

        sessionInfos.remove(jwtToken);
    }





    public List<SessionInfo> getAllSessionInfo() {
        try {
            sessionInfos.forEach(
                (s, sessionInfo) -> {
                    if (isTokenExpired(sessionInfo)) removeSession(s);
                }
            );
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
            if (jwtToken == null || sessionInfos.isEmpty()) return null;

            if (sessionInfos.get(jwtToken) != null) {
                SessionInfo sessionInfo = sessionInfos.get(jwtToken);
                if (isTokenExpired(sessionInfo)) removeSession(jwtToken);
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
        if (key == null || key.isBlank()) {
            key = "unknown";
        }

        Bucket bucket = loginBuckets.computeIfAbsent(key, ignored ->
            Bucket.builder()
                .addLimit(
                    Bandwidth.builder()
                        .capacity(10)
                        .refillGreedy(10, Duration.ofMinutes(1))
                        .build()
                )
                .build()
        );

        return bucket.tryConsume(1);
    }

}
