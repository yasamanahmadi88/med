package com.behsa.medportal.security.jwt;

import io.github.bucket4j.Bucket;
import java.time.LocalDateTime;

public class SessionInfo {

    private Object principal;
    private String sessionId;
    private String ip;
    private String username;
    /**
     * SHA-256 hash (hex) of the bearer token this session was created for, never the raw token.
     * Keeping the raw token here would leak live credentials into any heap dump.
     */
    private String tokenHash;
    private String userAgent;
    private LocalDateTime loginDate;
    private LocalDateTime logoutDate;
    private Boolean validToken;
    private LocalDateTime lastActionDate;
    private Bucket bucketPost; // for POST requests
    private Bucket bucketGet; //for other requests
    /** Inactivity timeout in minutes for this session (remember-me may be longer). */
    private long inactivityMinutes;

    public SessionInfo(
            Object principal,
            String sessionId,
            String ip,
            String username,
            String tokenHash,
            String userAgent,
            LocalDateTime loginDate,
            LocalDateTime logoutDate,
            Boolean validToken,
            LocalDateTime lastActionDate,
            Bucket bucketGet,
            Bucket bucketPost
    ) {
        this(principal, sessionId, ip, username, tokenHash, userAgent, loginDate, logoutDate, validToken, lastActionDate, bucketGet, bucketPost, 30);
    }

    public SessionInfo(
            Object principal,
            String sessionId,
            String ip,
            String username,
            String tokenHash,
            String userAgent,
            LocalDateTime loginDate,
            LocalDateTime logoutDate,
            Boolean validToken,
            LocalDateTime lastActionDate,
            Bucket bucketGet,
            Bucket bucketPost,
            long inactivityMinutes
    ) {
        this.principal = principal;
        this.sessionId = sessionId;
        this.ip = ip;
        this.username = username;
        this.tokenHash = tokenHash;
        this.userAgent = userAgent;
        this.loginDate = loginDate;
        this.logoutDate = logoutDate;
        this.validToken = validToken;
        this.lastActionDate = lastActionDate;
        this.bucketGet = bucketGet;
        this.bucketPost = bucketPost;
        this.inactivityMinutes = inactivityMinutes > 0 ? inactivityMinutes : 30;
    }

    public Object getPrincipal() {
        return principal;
    }

    public SessionInfo setPrincipal(Object principal) {
        this.principal = principal;
        return this;
    }

    public String getSessionId() {
        return sessionId;
    }

    public SessionInfo setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    /**
     * @return the SHA-256 hash (hex) of this session's bearer token. Safe to log or display; it can
     *         be matched against {@code SecurityCache.hashToken(token)} but cannot be replayed.
     */
    public String getTokenHash() {
        return tokenHash;
    }

    public SessionInfo setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
        return this;
    }

    /**
     * @return a short, non-reversible identifier for this session's token, suitable for a
     *         session-management screen.
     */
    public String getTokenHashPrefix() {
        return tokenHash == null ? null : tokenHash.substring(0, Math.min(12, tokenHash.length()));
    }

    public LocalDateTime getLoginDate() {
        return loginDate;
    }

    public SessionInfo setLoginDate(LocalDateTime loginDate) {
        this.loginDate = loginDate;
        return this;
    }

    public LocalDateTime getLogoutDate() {
        return logoutDate;
    }

    public SessionInfo setLogoutDate(LocalDateTime logoutDate) {
        this.logoutDate = logoutDate;
        return this;
    }

    public Boolean getValidToken() {
        return validToken;
    }

    public SessionInfo setValidToken(Boolean validToken) {
        this.validToken = validToken;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIp() {
        return ip;
    }

    public SessionInfo setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getLastActionDate() {
        return lastActionDate;
    }

    public SessionInfo setLastActionDate(LocalDateTime lastActionDate) {
        this.lastActionDate = lastActionDate;
        return this;
    }

    public Bucket getBucketGet() {
        return bucketGet;
    }

    public void setBucketGet(Bucket bucketGet) {
        this.bucketGet = bucketGet;
    }

    public Bucket getBucketPost() {
        return bucketPost;
    }

    public void setBucketPost(Bucket bucketPost) {
        this.bucketPost = bucketPost;
    }

    public long getInactivityMinutes() {
        return inactivityMinutes;
    }

    public SessionInfo setInactivityMinutes(long inactivityMinutes) {
        this.inactivityMinutes = inactivityMinutes;
        return this;
    }
}
