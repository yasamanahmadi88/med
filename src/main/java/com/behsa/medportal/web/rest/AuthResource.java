package com.behsa.medportal.web.rest;

import com.behsa.medportal.security.AuthoritiesConstants;
import com.behsa.medportal.security.SecurityCache;
import com.behsa.medportal.security.captcha.CaptchaValidationService;
import com.behsa.medportal.security.jwt.JWTFilter;
import com.behsa.medportal.security.jwt.TokenProvider;
import com.behsa.medportal.web.rest.vm.LoginVM;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthResource {

    private static final Logger log = LoggerFactory.getLogger(AuthResource.class);

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenProvider tokenProvider;
    private final CaptchaValidationService captchaValidationService;
    private final SecurityCache securityCache;

    public AuthResource(
        AuthenticationManagerBuilder authenticationManagerBuilder,
        TokenProvider tokenProvider,
        CaptchaValidationService captchaValidationService,
        SecurityCache securityCache
    ) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.tokenProvider = tokenProvider;
        this.captchaValidationService = captchaValidationService;
        this.securityCache = securityCache;
    }

    /**
     * Authenticate user and issue JWT token.
     * Enforces rate limiting per IP+username, validates captcha, and applies account lockout for admin users.
     *
     * @param loginVM login credentials and captcha data
     * @param request HTTP request containing client IP and user-agent
     * @return JWT token on successful authentication, 401 Unauthorized on failure, 429 Too Many Requests on rate limit
     */
    @PostMapping("/authenticate")
    public ResponseEntity<JWTToken> authorize(@Valid @RequestBody LoginVM loginVM, HttpServletRequest request) {
        String username = normalizeUsername(loginVM.getUsername());
        String clientIp = getClientIp(request);
        String loginRateLimitKey = clientIp + ":" + username;

        try {
            if (!securityCache.tryConsumeLogin(loginRateLimitKey)) {
                log.warn("Login rate limit exceeded for user: {} from IP: {}", username, clientIp);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
            }

            captchaValidationService.validate(
                loginVM.getCaptchaId(),
                loginVM.getCaptchaToken(),
                clientIp
            );

            UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                    username,
                    loginVM.getPassword()
                );

            Authentication authentication = authenticationManagerBuilder
                .getObject()
                .authenticate(authenticationToken);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            if (isAdmin(authentication)) {
                securityCache.removeSessionsByUsername(username);
                log.info("Existing admin sessions removed for user: {}", username);
            }

            boolean rememberMe = Boolean.TRUE.equals(loginVM.getRememberMe());
            String jwt = tokenProvider.createToken(authentication, rememberMe);
            // Both windows come from user-session.expire.* so the server-side session and the JWT
            // lifetime stay in step; see SecurityCache for the remember-me alignment note.
            long inactivityMinutes = rememberMe
                ? securityCache.getRememberMeInactivityMinutes()
                : securityCache.getDefaultInactivityMinutes();

            securityCache.storeSession(
                authentication.getPrincipal(),
                UUID.randomUUID().toString(),
                clientIp,
                username,
                jwt,
                request.getHeader("User-Agent"),
                LocalDateTime.now(),
                null,
                Boolean.TRUE,
                inactivityMinutes
            );

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);

            log.info("User authenticated successfully: {}", username);

            return new ResponseEntity<>(new JWTToken(jwt), httpHeaders, HttpStatus.OK);
        } catch (AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            log.debug("Authentication failed for user: {}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Normalize username to lowercase for consistent comparison and storage.
     *
     * @param username raw username input
     * @return normalized username (lowercase, trimmed) or empty string if null/blank
     */
    private String normalizeUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return "";
        }

        return username.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Extract client IP address, preventing X-Forwarded-For spoofing attacks.
     * Only trusts X-Forwarded-For header when the direct client connection is from localhost (127.0.0.1 or ::1).
     * This prevents attackers from bypassing rate limiting by forging the X-Forwarded-For header.
     * CWE-307: Improper Restriction of Excessive Authentication Attempts.
     *
     * @param request HTTP request with potential X-Forwarded-For header
     * @return client IP address: X-Forwarded-For if trusted proxy, otherwise remoteAddr
     */
    private String getClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        // Only trust X-Forwarded-For if it comes from localhost (127.0.0.1, ::1).
        // Without explicit trusted-proxy configuration, direct client requests must not be spoofed.
        if (xForwardedFor != null && !xForwardedFor.isEmpty() &&
            ("127.0.0.1".equals(remoteAddr) || "::1".equals(remoteAddr))) {
            return xForwardedFor.split(",")[0].trim();
        }
        return remoteAddr;
    }

    /**
     * Check if the authenticated user has admin authority.
     *
     * @param authentication user's authentication object with granted authorities
     * @return true if user has ROLE_ADMIN, false otherwise
     */
    private boolean isAdmin(Authentication authentication) {
        return authentication
            .getAuthorities()
            .stream()
            .anyMatch(authority -> AuthoritiesConstants.ADMIN.equals(authority.getAuthority()));
    }

    public static class JWTToken {

        private String idToken;

        public JWTToken(String idToken) {
            this.idToken = idToken;
        }

        @JsonProperty("id_token")
        public String getIdToken() {
            return idToken;
        }

        public void setIdToken(String idToken) {
            this.idToken = idToken;
        }
    }
}
