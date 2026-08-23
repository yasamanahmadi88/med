package com.behsa.medportal.web.rest;

import com.behsa.medportal.security.SecurityCache;
import com.behsa.medportal.security.jwt.JWTFilter;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String JWT_COOKIE_NAME = "JWT";

    private final SecurityCache securityCache;

    public AuthController(SecurityCache securityCache) {
        this.securityCache = securityCache;
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String jwt = resolveToken(request);

        if (StringUtils.hasText(jwt)) {
            securityCache.removeSession(jwt);
        }

        SecurityContextHolder.clearContext();

        ResponseCookie expiredJwtCookie = ResponseCookie
            .from(JWT_COOKIE_NAME, "")
            .httpOnly(true)
            .secure(request.isSecure())
            .sameSite("Strict")
            .path("/")
            .maxAge(0)
            .build();

        return ResponseEntity
            .noContent()
            .header(HttpHeaders.SET_COOKIE, expiredJwtCookie.toString())
            .build();
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(JWTFilter.AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
