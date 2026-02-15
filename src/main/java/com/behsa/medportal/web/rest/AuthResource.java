package com.behsa.medportal.web.rest;

// … imports omitted for brevity …

import com.behsa.medportal.repository.UserRepository;
import com.behsa.medportal.security.SecurityCache;
import com.behsa.medportal.security.captcha.CaptchaValidationService;
import com.behsa.medportal.security.jwt.JWTFilter;
import com.behsa.medportal.security.jwt.SessionInfo;
import com.behsa.medportal.security.jwt.TokenProvider;
import com.behsa.medportal.web.rest.vm.LoginVM;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

import static com.behsa.medportal.security.AuthoritiesConstants.ADMIN;

@RestController
@RequestMapping("/api")
public class AuthResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthResource.class);
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenProvider tokenProvider;
    private final CaptchaValidationService captchaValidationService;
    private final SecurityCache securityCache;
    private final UserRepository userRepository;


    public AuthResource(AuthenticationManagerBuilder authenticationManagerBuilder,
                        TokenProvider tokenProvider,
                        CaptchaValidationService captchaValidationService,
                        SecurityCache securityCache,
                        UserRepository userRepository) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.tokenProvider = tokenProvider;
        this.captchaValidationService = captchaValidationService;
        this.securityCache = securityCache;
        this.userRepository = userRepository;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<JWTToken> authorize(@Valid @RequestBody LoginVM loginVM,
                                              HttpServletRequest request,
                                              HttpServletResponse response) {

        /* ─── 1  Validate captcha before doing anything else ─── */
        captchaValidationService.validate(
            loginVM.getCaptchaToken(),
            request.getRemoteAddr()
        );

        /* ─── 2  Proceed with normal username/password auth ─── */
        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(
                loginVM.getUsername(),
                loginVM.getPassword()
            );

        String userAgent = request.getHeader("user-agent");

        Authentication authentication =
            authenticationManagerBuilder.getObject().authenticate(authToken);

        List<String> userAuthorityList = userRepository.findAllAuthoritiesByLoginName(loginVM.getUsername());

        if (userAuthorityList.contains(ADMIN) && securityCache.hasConcurrentSession(loginVM.getUsername())) {
            handleConcurrentSession(loginVM.getUsername(), userAgent);
        }

        validateLoginAttempts(loginVM.getUsername(), request);//TODO complete audit event


        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.createToken(authentication, loginVM.getRememberMe());

        securityCache.storeSession(
            authentication.getPrincipal(),
            request.getSession().getId(),
            request.getRemoteAddr(),
            loginVM.getUsername(),
            jwt,
            userAgent,
            LocalDateTime.now(),
            null,
            Boolean.TRUE
        );

        HttpHeaders headers = new HttpHeaders();
        headers.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
        LOGGER.info("Session  for user: {}", securityCache.fetchSessionInfo(loginVM.getUsername()));
        addSameSiteCookieAttribute(request, response);
        return new ResponseEntity<>(new JWTToken(jwt), headers, HttpStatus.OK);
    }

    private void validateLoginAttempts(String username, HttpServletRequest request) {

/*
        List<PersistentAuditEvent> persistentAuditEvents = auditEventService.findTopFiveAuthenticationFailureStateWithLoginUserNameInDuration(username, LocalDateTime.now().minusMinutes(10), LocalDateTime.now());
        if (!persistentAuditEvents.isEmpty() && persistentAuditEvents.size() >= 5) {
            throw new InvalidLoginAttemptException();
        }

        List<PersistentAuditEvent> persistentAuditEventsFromRemoteHost = auditEventService.findTopFiveStateExceptLogoutWithLoginUserNameAndRemoteHostInDuration(username, request.getRemoteHost(), LocalDateTime.now().minusMinutes(10), LocalDateTime.now());
        if (!persistentAuditEventsFromRemoteHost.isEmpty() && persistentAuditEventsFromRemoteHost.size() >= 5) {
            throw new InvalidLoginAttemptException();
        }
*/

    }

    private void addSameSiteCookieAttribute(HttpServletRequest request, HttpServletResponse response) {
        response.getHeaders(HttpHeaders.SET_COOKIE).forEach(header ->
            response.addHeader(HttpHeaders.SET_COOKIE, header + "; Path=/; SameSite=Strict; HttpOnly" + (request.isSecure() ? "; Secure" : "")));
    }

    static class JWTToken {
        private String idToken;

        JWTToken(String idToken) {
            this.idToken = idToken;
        }

        @JsonProperty("id_token")
        String getIdToken() {
            return idToken;
        }

        void setIdToken(String idToken) {
            this.idToken = idToken;
        }
    }

    private void handleConcurrentSession(String username, String userAgent) {
        SessionInfo existingSession = securityCache.fetchSessionInfo(username);
        if (existingSession != null) {
            securityCache.removeSession(existingSession.getJwtToken());
            LOGGER.info("Session removed for user: {}", username);
        }
    }

}
