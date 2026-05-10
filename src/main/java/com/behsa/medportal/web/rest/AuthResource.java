package com.behsa.medportal.web.rest;

import com.behsa.medportal.security.captcha.CaptchaValidationService;
import com.behsa.medportal.security.jwt.JWTFilter;
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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api")
public class AuthResource {

    private static final Logger log = LoggerFactory.getLogger(AuthResource.class);

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    private final TokenProvider tokenProvider;

    private final CaptchaValidationService captchaValidationService;

    public AuthResource(
        AuthenticationManagerBuilder authenticationManagerBuilder,
        TokenProvider tokenProvider,
        CaptchaValidationService captchaValidationService
    ) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.tokenProvider = tokenProvider;
        this.captchaValidationService = captchaValidationService;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<JWTToken> authorize(@Valid @RequestBody LoginVM loginVM, HttpServletRequest request) {
        try {
            captchaValidationService.validate(
                loginVM.getCaptchaId(),
                loginVM.getCaptchaToken(),
                request.getRemoteAddr()
            );

            UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                    loginVM.getUsername(),
                    loginVM.getPassword()
                );

            Authentication authentication = authenticationManagerBuilder
                .getObject()
                .authenticate(authenticationToken);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = tokenProvider.createToken(
                authentication,
                Boolean.TRUE.equals(loginVM.getRememberMe())
            );

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);

            return new ResponseEntity<>(new JWTToken(jwt), httpHeaders, HttpStatus.OK);
        } catch (AuthenticationException ex) {
            SecurityContextHolder.clearContext();

            log.debug("Authentication failed.");

            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build();
        }
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
