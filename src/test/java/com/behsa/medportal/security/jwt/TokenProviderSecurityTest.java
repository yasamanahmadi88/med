package com.behsa.medportal.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.behsa.medportal.domain.Authority;
import com.behsa.medportal.domain.User;
import com.behsa.medportal.management.SecurityMetersService;
import com.behsa.medportal.repository.UserRepository;
import com.behsa.medportal.security.AuthoritiesConstants;
import com.behsa.medportal.security.PortalUser;
import com.behsa.medportal.service.ResourceAuthorityQueryService;
import com.behsa.medportal.service.dto.ResourceAuthorityDTO;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import tech.jhipster.config.JHipsterProperties;

/**
 * Security-focused unit tests for JWT creation and validation.
 */
class TokenProviderSecurityTest {

    private static final String BASE64_SECRET =
        "fd54a45s65fds737b9aafcb3412e07ed99b267f33413274720ddbb7f6c5e64e9f14075f2d7ed041592f0b7657baf8";

    private TokenProvider tokenProvider;
    private SecretKey key;
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        JHipsterProperties props = new JHipsterProperties();
        props.getSecurity().getAuthentication().getJwt().setBase64Secret(BASE64_SECRET);
        props.getSecurity().getAuthentication().getJwt().setTokenValidityInSeconds(60);
        props.getSecurity().getAuthentication().getJwt().setTokenValidityInSecondsForRememberMe(3600);

        SecurityMetersService meters = new SecurityMetersService(new SimpleMeterRegistry());
        ResourceAuthorityQueryService resourceAuthorityQueryService = mock(ResourceAuthorityQueryService.class);
        when(resourceAuthorityQueryService.findByAuthorities(anyList(), any(Pageable.class))).thenReturn(
            new PageImpl<>(List.<ResourceAuthorityDTO>of())
        );

        userRepository = mock(UserRepository.class);
        tokenProvider = new TokenProvider(props, meters, resourceAuthorityQueryService, userRepository);
        key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(BASE64_SECRET));
    }

    @Test
    void validTokenIsAccepted() {
        String token = tokenProvider.createToken(createAuthentication("admin"), false);
        assertThat(tokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void missingTokenIsRejected() {
        assertThat(tokenProvider.validateToken("")).isFalse();
        assertThat(tokenProvider.validateToken(null)).isFalse();
    }

    @Test
    void malformedTokenIsRejected() {
        String token = tokenProvider.createToken(createAuthentication("admin"), false);
        assertThat(tokenProvider.validateToken(token.substring(2))).isFalse();
    }

    @Test
    void expiredTokenIsRejected() {
        ReflectionTestUtils.setField(tokenProvider, "tokenValidityInMilliseconds", -60_000L);
        String token = tokenProvider.createToken(createAuthentication("admin"), false);
        assertThat(tokenProvider.validateToken(token)).isFalse();
    }

    @Test
    void tokenWithWrongSignatureIsRejected() {
        SecretKey otherKey = Keys.hmacShaKeyFor(
            Decoders.BASE64.decode("Xfd54a45s65fds737b9aafcb3412e07ed99b267f33413274720ddbb7f6c5e64e9f14075f2d7ed041592f0b7657baf8")
        );
        String forged = Jwts.builder()
            .subject("admin")
            .claim("auth", AuthoritiesConstants.ADMIN)
            .signWith(otherKey, Jwts.SIG.HS512)
            .expiration(Date.from(Instant.now().plusSeconds(60)))
            .compact();
        assertThat(tokenProvider.validateToken(forged)).isFalse();
    }

    @Test
    void algNoneTokenIsRejected() {
        // Unsigned / alg=none style payload must not validate against HMAC parser.
        String noneToken =
            "eyJhbGciOiJub25lIn0." +
            "eyJzdWIiOiJhZG1pbiIsImF1dGgiOiJST0xFX0FETUlOIiwiZXhwIjo0MTAyNDQ0ODAwfQ.";
        assertThat(tokenProvider.validateToken(noneToken)).isFalse();
    }

    @Test
    void getAuthenticationLoadsAuthoritiesFromActiveUser() {
        User user = new User();
        user.setLogin("admin");
        user.setActivated(true);
        user.setPartyId("P1");
        Authority authority = new Authority();
        authority.setName(AuthoritiesConstants.ADMIN);
        Set<Authority> authorities = new HashSet<>();
        authorities.add(authority);
        user.setAuthorities(authorities);
        when(userRepository.findOneWithAuthoritiesByLoginAndActivatedTrue("admin")).thenReturn(Optional.of(user));

        String token = tokenProvider.createToken(createAuthentication("admin"), false);
        Authentication authentication = tokenProvider.getAuthentication(token);
        assertThat(authentication.getAuthorities()).extracting("authority").contains(AuthoritiesConstants.ADMIN);
        assertThat(authentication.getName()).isEqualTo("admin");
    }

    private Authentication createAuthentication(String login) {
        PortalUser principal = new PortalUser(
            login,
            "",
            true,
            true,
            true,
            true,
            List.of(new SimpleGrantedAuthority(AuthoritiesConstants.ADMIN)),
            "party-1",
            List.of(),
            null
        );
        return new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());
    }
}
