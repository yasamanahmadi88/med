package com.behsa.medportal.security.jwt;

import com.behsa.medportal.management.SecurityMetersService;
import com.behsa.medportal.security.PortalUser;
import com.behsa.medportal.service.ResourceAuthorityQueryService;
import com.behsa.medportal.service.dto.ResourceAuthorityDTO;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import tech.jhipster.config.JHipsterProperties;
import com.behsa.medportal.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import com.behsa.medportal.domain.Authority;

@Component
public class TokenProvider {

    private final Logger log = LoggerFactory.getLogger(TokenProvider.class);

    private static final String AUTHORITIES_KEY = "auth";
    private static final String PARTY_ID_KEY = "PartyId";
    static final String TOKEN_ISSUER = "MedPortal";
    static final String TOKEN_AUDIENCE = "medportal-api";

    private static final String INVALID_JWT_TOKEN = "Invalid JWT token.";

    private final SecretKey key;

    private final JwtParser jwtParser;

    private final long tokenValidityInMilliseconds;
    private final ResourceAuthorityQueryService resourceAuthorityQueryService;

    private final long tokenValidityInMillisecondsForRememberMe;

    private final SecurityMetersService securityMetersService;

    private final UserRepository userRepository;


    public TokenProvider(JHipsterProperties jHipsterProperties, SecurityMetersService securityMetersService,
                         ResourceAuthorityQueryService resourceAuthorityQueryService, UserRepository userRepository) {

        this.resourceAuthorityQueryService = resourceAuthorityQueryService;
        this.userRepository = userRepository;

        byte[] keyBytes;
        String secret = jHipsterProperties.getSecurity().getAuthentication().getJwt().getBase64Secret();
        if (!ObjectUtils.isEmpty(secret)) {
            log.debug("Using a Base64-encoded JWT secret key");
            keyBytes = Decoders.BASE64.decode(secret);
        } else {
            log.warn(
                "Warning: the JWT key used is not Base64-encoded. " +
                "We recommend using the `jhipster.security.authentication.jwt.base64-secret` key for optimum security."
            );
            secret = jHipsterProperties.getSecurity().getAuthentication().getJwt().getSecret();
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        key = Keys.hmacShaKeyFor(keyBytes);
        // verifyWith binds an HMAC key so alg=none / asymmetric alg confusion is rejected.
        jwtParser = Jwts
            .parser()
            .verifyWith(key)
            .requireIssuer(TOKEN_ISSUER)
            .requireAudience(TOKEN_AUDIENCE)
            .clockSkewSeconds(30)
            .build();
        this.tokenValidityInMilliseconds = 1000 * jHipsterProperties.getSecurity().getAuthentication().getJwt().getTokenValidityInSeconds();
        this.tokenValidityInMillisecondsForRememberMe =
            1000 * jHipsterProperties.getSecurity().getAuthentication().getJwt().getTokenValidityInSecondsForRememberMe();

        this.securityMetersService = securityMetersService;
    }

    public String createToken(Authentication authentication, boolean rememberMe) {
        String authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validity;
        if (rememberMe) {
            validity = new Date(now + this.tokenValidityInMillisecondsForRememberMe);
        } else {
            validity = new Date(now + this.tokenValidityInMilliseconds);
        }

        PortalUser myUser = (PortalUser) authentication.getPrincipal();
        // Always emit PartyId (empty string when unset). Null party_id is common for seeded users;
        // omitting the claim made validateToken() fail and broke every authenticated API/menu load.
        String partyId = myUser.getPartyId() != null ? myUser.getPartyId() : "";

        return Jwts
            .builder()
            .issuer(TOKEN_ISSUER)
            .audience()
            .add(TOKEN_AUDIENCE)
            .and()
            .subject(authentication.getName())
            .claim(AUTHORITIES_KEY, authorities)
            .claim(PARTY_ID_KEY, partyId)
            .signWith(key, Jwts.SIG.HS512)
            .expiration(validity)
            .compact();
    }

//    public Authentication getAuthentication(String token) {
//        Claims claims = jwtParser.parseSignedClaims(token).getPayload();
//
//        Collection<? extends GrantedAuthority> authorities = Arrays
//            .stream(claims.get(AUTHORITIES_KEY).toString().split(","))
//            .filter(auth -> !auth.trim().isEmpty())
//            .map(SimpleGrantedAuthority::new)
//            .collect(Collectors.toList());
//
//        List<ResourceAuthorityDTO> resourceAuthorities = fetchResourceAuthorities(authorities);
//        PortalUser principal = new PortalUser(
//            claims.getSubject(),
//            "",
//            true,
//            true,
//            true,
//            true,
//            authorities,
//            ((String) claims.get(PARTY_ID_KEY)),
//            resourceAuthorities,
//            null
//        );
//
//        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
//    }

    /**
     * Builds the {@link Authentication} for a bearer token, parsing (and therefore signature-verifying)
     * it first. Prefer {@link #getAuthentication(String, Claims)} when the caller has already obtained
     * verified claims from {@link #parseAndValidate(String)} — that avoids a second signature check.
     */
    public Authentication getAuthentication(String token) {
        Claims claims = jwtParser.parseSignedClaims(token).getPayload();

        return getAuthentication(token, claims);
    }

    /**
     * Builds the {@link Authentication} from claims that have ALREADY been signature-verified by
     * {@link #parseAndValidate(String)} or {@link #getAuthentication(String)}. Never pass unverified
     * claims here.
     *
     * @param token  the raw token, kept as the authentication credentials (as before).
     * @param claims the verified claims for {@code token}.
     */
    public Authentication getAuthentication(String token, Claims claims) {
        String login = claims.getSubject();

        com.behsa.medportal.domain.User user = userRepository
            .findOneWithAuthoritiesByLoginAndActivatedTrue(login)
            .orElseThrow(() -> new BadCredentialsException("User is not active or no longer exists."));

        Collection<? extends GrantedAuthority> authorities = user
            .getAuthorities()
            .stream()
            .map(Authority::getName)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

        List<ResourceAuthorityDTO> resourceAuthorities = fetchResourceAuthorities(authorities);

        String partyId = user.getPartyId() != null ? user.getPartyId() : "";
        PortalUser principal = new PortalUser(
            user.getLogin(),
            "",
            user.isActivated(),
            true,
            true,
            true,
            authorities,
            partyId,
            resourceAuthorities,
            user
        );

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    /**
     * Parses and fully verifies a bearer token exactly once and returns its claims.
     * <p>
     * The parse is signature-verifying ({@code parseSignedClaims} bound to the HMAC key, with the
     * issuer and audience required), and the required-claims check still runs. Every failure mode is
     * routed into {@code securityMetersService} exactly as before.
     *
     * @return the verified claims, or {@code null} if the token is absent, malformed, expired,
     *         unsupported, badly signed, or missing a required claim.
     */
    public Claims parseAndValidate(String authToken) {
        if (authToken == null || authToken.isBlank()) {
            return null;
        }
        try {
            Claims claims = jwtParser.parseSignedClaims(authToken).getPayload();
            if (!hasRequiredClaims(claims)) {
                log.trace("Invalid JWT token because required claims are missing.");
                return null;
            }

            return claims;
        } catch (ExpiredJwtException e) {
            this.securityMetersService.trackTokenExpired();

            log.trace(INVALID_JWT_TOKEN, e);
        } catch (UnsupportedJwtException e) {
            this.securityMetersService.trackTokenUnsupported();

            log.trace(INVALID_JWT_TOKEN, e);
        } catch (MalformedJwtException e) {
            this.securityMetersService.trackTokenMalformed();

            log.trace(INVALID_JWT_TOKEN, e);
        } catch (SignatureException e) {
            this.securityMetersService.trackTokenInvalidSignature();

            log.trace(INVALID_JWT_TOKEN, e);
        } catch (JwtException e) {
            log.trace(INVALID_JWT_TOKEN, e);
        } catch (IllegalArgumentException e) {
            log.error("Token validation error {}", e.getMessage());
        }

        return null;
    }

    /**
     * @return {@code true} when the token parses, verifies and carries the required claims.
     *         Kept for callers that only need a yes/no answer; request-path callers should use
     *         {@link #parseAndValidate(String)} so the token is parsed only once.
     */
    public boolean validateToken(String authToken) {
        return parseAndValidate(authToken) != null;
    }

    private boolean hasRequiredClaims(Claims claims) {
        // PartyId may be blank for users without a party binding, but the claim must be present.
        return StringUtils.hasText(claims.getSubject()) &&
            StringUtils.hasText(claims.get(AUTHORITIES_KEY, String.class)) &&
            claims.get(PARTY_ID_KEY) != null;
    }

    private List<ResourceAuthorityDTO> fetchResourceAuthorities(Collection<? extends GrantedAuthority> authorities) {
        List<String> ids = new ArrayList<>();
        authorities.forEach(
            grantedAuthority -> {
                ids.add(grantedAuthority.getAuthority());
            }
        );
        return resourceAuthorityQueryService.findByAuthorities(ids, Pageable.unpaged()).getContent();
    }
}
