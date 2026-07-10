package com.behsa.medportal.security.jwt;

import com.behsa.medportal.security.SecurityCache;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

/**
 * Filters incoming requests and installs a Spring Security principal
 * if a valid Bearer token is present and the token is active in server session cache.
 */
public class JWTFilter extends GenericFilterBean {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    private static final Logger log = LoggerFactory.getLogger(JWTFilter.class);

    private final TokenProvider tokenProvider;

    private final SecurityCache securityCache;

    public JWTFilter(TokenProvider tokenProvider, SecurityCache securityCache) {
        this.tokenProvider = tokenProvider;
        this.securityCache = securityCache;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String jwt = resolveToken(request);

        try {
            if (!StringUtils.hasText(jwt)) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }

            if (!tokenProvider.validateToken(jwt)) {
                SecurityContextHolder.clearContext();
                removeInvalidSession(jwt);
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }

            SessionInfo sessionInfo = securityCache.getSessionInfoByToken(jwt);

            if (!isActiveSession(sessionInfo)) {
                SecurityContextHolder.clearContext();
                removeInvalidSession(jwt);
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }

            if (isRateLimitExceeded(request, sessionInfo)) {
                SecurityContextHolder.clearContext();
                rejectTooManyRequests(servletResponse);
                return;
            }

            Authentication authentication = tokenProvider.getAuthentication(jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (RuntimeException ex) {
            SecurityContextHolder.clearContext();
            removeInvalidSession(jwt);
            log.debug("JWT authentication failed: {}", ex.getMessage());
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private boolean isActiveSession(SessionInfo sessionInfo) {
        return sessionInfo != null && Boolean.TRUE.equals(sessionInfo.getValidToken());
    }

    private void removeInvalidSession(String jwt) {
        if (StringUtils.hasText(jwt)) {
            securityCache.removeSession(jwt);
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    private boolean isRateLimitExceeded(HttpServletRequest request, SessionInfo sessionInfo) {
        if (sessionInfo == null) {
            return false;
        }

        String method = request.getMethod();

        if ("GET".equalsIgnoreCase(method)) {
            return sessionInfo.getBucketGet() != null && !sessionInfo.getBucketGet().tryConsume(1);
        }

        if (
            "POST".equalsIgnoreCase(method) ||
                "PUT".equalsIgnoreCase(method) ||
                "PATCH".equalsIgnoreCase(method) ||
                "DELETE".equalsIgnoreCase(method)
        ) {
            return sessionInfo.getBucketPost() != null && !sessionInfo.getBucketPost().tryConsume(1);
        }

        return false;
    }

    private void rejectTooManyRequests(ServletResponse servletResponse) throws IOException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"too many requests\"}");
    }
}
