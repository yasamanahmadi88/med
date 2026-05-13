package com.behsa.medportal.security.jwt;

import com.behsa.medportal.security.SecurityCache;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class JWTConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private final TokenProvider tokenProvider;
    private final SecurityCache securityCache;

    public JWTConfigurer(TokenProvider tokenProvider, SecurityCache securityCache) {
        this.tokenProvider = tokenProvider;
        this.securityCache = securityCache;
    }

    @Override
    public void configure(HttpSecurity http) {
        JWTFilter customFilter = new JWTFilter(tokenProvider, securityCache);
        http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);

    }
}
