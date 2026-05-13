package com.behsa.medportal.security.jwt;

import com.behsa.medportal.security.SecurityCache;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

/**
 * Filters incoming requests and installs a Spring Security principal
 * if a valid Bearer token is present.
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

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String jwt = resolveToken(httpServletRequest);

        try {
            if (StringUtils.hasText(jwt)) {
                if (tokenProvider.validateToken(jwt) && isTokenActiveInServerSession(jwt)) {
                    Authentication authentication = tokenProvider.getAuthentication(jwt);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    SecurityContextHolder.clearContext();
                    removeInvalidSession(jwt);
                }
            }
        } catch (RuntimeException ex) {
            SecurityContextHolder.clearContext();
            removeInvalidSession(jwt);
            log.debug("JWT authentication failed: {}", ex.getMessage());
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private boolean isTokenActiveInServerSession(String jwt) {
        SessionInfo sessionInfo = securityCache.getSessionInfoByToken(jwt);
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
}
