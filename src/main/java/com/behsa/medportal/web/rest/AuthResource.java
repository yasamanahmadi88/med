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
            long inactivityMinutes = rememberMe
                ? SecurityCache.REMEMBER_ME_INACTIVITY_MINUTES
                : SecurityCache.DEFAULT_INACTIVITY_MINUTES;

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

    private String normalizeUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return "";
        }

        return username.trim().toLowerCase(Locale.ROOT);
    }

    private String getClientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

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
