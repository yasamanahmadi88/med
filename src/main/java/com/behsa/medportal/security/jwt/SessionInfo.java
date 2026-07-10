package com.behsa.medportal.security.jwt;

import io.github.bucket4j.Bucket;
import java.time.LocalDateTime;

public class SessionInfo {

    private Object principal;
    private String sessionId;
    private String ip;
    private String username;
    private String jwtToken;
    private String userAgent;
    private LocalDateTime loginDate;
    private LocalDateTime logoutDate;
    private Boolean validToken;
    private LocalDateTime lastActionDate;
    private Bucket bucketPost; // for POST requests
    private Bucket bucketGet; //for other requests

    public SessionInfo(
            Object principal,
            String sessionId,
            String ip,
            String username,
            String jwtToken,
            String userAgent,
            LocalDateTime loginDate,
            LocalDateTime logoutDate,
            Boolean validToken,
            LocalDateTime lastActionDate,
            Bucket bucketGet,
            Bucket bucketPost
    ) {
        this.principal = principal;
        this.sessionId = sessionId;
        this.ip = ip;
        this.username = username;
        this.jwtToken = jwtToken;
        this.userAgent = userAgent;
        this.loginDate = loginDate;
        this.logoutDate = logoutDate;
        this.validToken = validToken;
        this.lastActionDate = lastActionDate;
        this.bucketGet = bucketGet;
        this.bucketPost = bucketPost;
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

    public String getJwtToken() {
        return jwtToken;
    }

    public SessionInfo setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
        return this;
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
}
