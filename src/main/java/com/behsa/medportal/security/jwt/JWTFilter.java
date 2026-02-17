package com.behsa.medportal.security.jwt;

import com.behsa.medportal.security.SecurityCache;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filters incoming requests and installs a Spring Security principal if a header corresponding to a valid user is
 * found.
 */
public class JWTFilter extends GenericFilterBean {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    private final TokenProvider tokenProvider;
    private final SecurityCache securityCache;

    public JWTFilter(TokenProvider tokenProvider, SecurityCache securityCache) {
        this.tokenProvider = tokenProvider;
        this.securityCache = securityCache;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {
        boolean flag = true;
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String jwt = resolveToken(httpServletRequest);
        SessionInfo sessionInfo = securityCache.getSessionInfoByToken(jwt);

        if (
            !httpServletRequest.getRequestURI().endsWith("api/cp-eyrtyertye") &&
                !httpServletRequest.getRequestURI().endsWith("api/authenticate") &&
                !httpServletRequest.getRequestURI().endsWith("management/info") &&
                !httpServletRequest.getRequestURI().endsWith("/captcha-endpoint") &&
                !httpServletRequest.getRequestURI().endsWith("api/captcha-validate") &&
                !httpServletRequest.getRequestURI().endsWith("api/captcha.png") &&
                !httpServletRequest.getRequestURI().endsWith("api/public/backUrl") &&
                !httpServletRequest.getRequestURI().endsWith("api/auth/logout") &&
                !httpServletRequest.getRequestURI().endsWith("error") &&
                !httpServletRequest.getRequestURI().endsWith("/login") &&
                !httpServletRequest.getRequestURI().endsWith(".js") &&
                !httpServletRequest.getRequestURI().endsWith(".html") &&
                !httpServletRequest.getRequestURI().endsWith(".woff2") &&
                !httpServletRequest.getRequestURI().endsWith(".css") &&
                !httpServletRequest.getRequestURI().equals("/")
        ) {
            if (jwt == null || sessionInfo == null) {
                HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
                httpResponse.setContentType("text/plain");
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.getWriter().append("error.npg.token.empty");
                httpResponse.sendRedirect("/login");//TODO redirect do not work properly
                flag = false;
            } else if (!sessionInfo.getValidToken()) {
                HttpServletResponse httpServletResponse = ((HttpServletResponse) servletResponse);
                httpServletResponse.setContentType("text/plain");
                httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpServletResponse.getWriter().append("error.portal.token.invalid");
                flag = false;
            }
        }

        if (StringUtils.hasText(jwt) && this.tokenProvider.validateToken(jwt)) {
            Authentication authentication = this.tokenProvider.getAuthentication(jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else if (StringUtils.hasText(jwt) && !this.tokenProvider.validateToken(jwt)) {
            SecurityContextHolder.getContext().setAuthentication(null);
            securityCache.removeSession(jwt);
        }

        if (sessionInfo != null) {
            boolean isRequestRateLimited = false;
            if (httpServletRequest.getMethod().equals("GET")) {
                isRequestRateLimited = !sessionInfo.getBucketGet().tryConsume(1);
            } else if (httpServletRequest.getMethod().equals("POST")) {
                isRequestRateLimited = !sessionInfo.getBucketPost().tryConsume(1);
            } else {
                isRequestRateLimited = !sessionInfo.getBucketGet().tryConsume(1);
            }

            if (isRequestRateLimited) {
                HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
                httpServletResponse.setContentType("text/plain");
                httpServletResponse.setStatus(429); // Too Many Requests
                httpServletResponse.getWriter().append("error.too.many.requests");
                flag = false;
            }
        }

        if (flag) filterChain.doFilter(servletRequest, servletResponse);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
