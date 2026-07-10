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
import tech.jhipster.config.JHipsterProperties;
import com.behsa.medportal.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import com.behsa.medportal.domain.Authority;

@Component
public class TokenProvider {

    private final Logger log = LoggerFactory.getLogger(TokenProvider.class);

    private static final String AUTHORITIES_KEY = "auth";
    private static final String PARTY_ID_KEY = "PartyId";

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
        jwtParser = Jwts.parser().verifyWith(key).clockSkewSeconds(30).build();
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
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put(PARTY_ID_KEY, myUser.getPartyId());

        return Jwts
            .builder()
            .subject(authentication.getName())
            .claim(AUTHORITIES_KEY, authorities)
            .claims(claims)
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

    public Authentication getAuthentication(String token) {
        Claims claims = jwtParser.parseSignedClaims(token).getPayload();

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

        PortalUser principal = new PortalUser(
            user.getLogin(),
            "",
            user.isActivated(),
            true,
            true,
            true,
            authorities,
            user.getPartyId(),
            resourceAuthorities,
            null
        );

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public boolean validateToken(String authToken) {
        if (authToken == null || authToken.isBlank()) {
            return false;
        }
        try {
            jwtParser.parseSignedClaims(authToken);

            return true;
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
        } catch (IllegalArgumentException e) {
            log.error("Token validation error {}", e.getMessage());
        }

        return false;
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
