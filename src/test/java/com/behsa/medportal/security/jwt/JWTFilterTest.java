package com.behsa.medportal.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.behsa.medportal.security.SecurityCache;
import com.behsa.medportal.security.AuthoritiesConstants;
import io.jsonwebtoken.Claims;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class JWTFilterTest {

    private TokenProvider tokenProvider;
    private SecurityCache securityCache;

    private JWTFilter jwtFilter;

    @BeforeEach
    void setup() {
        tokenProvider = mock(TokenProvider.class);
        securityCache = mock(SecurityCache.class);
        jwtFilter = new JWTFilter(tokenProvider, securityCache);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validBearerTokenInstallsAuthentication() throws Exception {
        String jwt = "good.jwt";
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            "test-user",
            "test-password",
            List.of(new SimpleGrantedAuthority(AuthoritiesConstants.USER))
        );
        Claims claims = mock(Claims.class);
        // The filter parses the token exactly once and reuses the verified claims.
        when(tokenProvider.parseAndValidate(jwt)).thenReturn(claims);
        when(securityCache.getSessionInfoByToken(jwt)).thenReturn(activeSession(jwt));
        when(tokenProvider.getAuthentication(jwt, claims)).thenReturn(authentication);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
        request.setRequestURI("/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        jwtFilter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("test-user");
    }

    @Test
    void invalidBearerTokenClearsAuthenticationAndRemovesSession() throws Exception {
        String jwt = "wrong.jwt";
        when(tokenProvider.parseAndValidate(jwt)).thenReturn(null);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
        request.setRequestURI("/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        jwtFilter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(securityCache).removeSession(jwt);
    }

    @Test
    void missingAuthorizationHeaderIsIgnored() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        jwtFilter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenProvider, never()).parseAndValidate("good.jwt");
    }

    @Test
    void emptyAuthorizationHeaderIsIgnored() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JWTFilter.AUTHORIZATION_HEADER, "");
        request.setRequestURI("/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        jwtFilter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenProvider, never()).parseAndValidate("");
    }

    @Test
    void emptyBearerTokenIsIgnored() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JWTFilter.AUTHORIZATION_HEADER, "Bearer ");
        request.setRequestURI("/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        jwtFilter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenProvider, never()).parseAndValidate("");
    }

    @Test
    void authorizationHeaderWithoutBearerSchemeIsIgnored() throws Exception {
        String jwt = "good.jwt";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JWTFilter.AUTHORIZATION_HEADER, jwt);
        request.setRequestURI("/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        jwtFilter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenProvider, never()).parseAndValidate(jwt);
    }

    @Test
    void tokenInQueryStringIsIgnored() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/account");
        request.setQueryString("access_token=good.jwt");
        request.addParameter("access_token", "good.jwt");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        jwtFilter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenProvider, never()).parseAndValidate("good.jwt");
    }

    private SessionInfo activeSession(String jwt) {
        return new SessionInfo(
            "test-user",
            "session-id",
            "127.0.0.1",
            "test-user",
            // SessionInfo holds the SHA-256 hash of the bearer token, never the raw token.
            SecurityCache.hashToken(jwt),
            "JUnit",
            LocalDateTime.now(),
            null,
            true,
            LocalDateTime.now(),
            null,
            null
        );
    }
}
