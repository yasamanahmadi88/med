package com.behsa.medportal.config;

import com.behsa.medportal.security.AuthoritiesConstants;
import com.behsa.medportal.security.jwt.JWTConfigurer;
import com.behsa.medportal.security.jwt.TokenProvider;
import com.behsa.medportal.security.*;
import com.behsa.medportal.security.jwt.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;
import tech.jhipster.config.JHipsterProperties;

import java.util.Arrays;

@EnableWebSecurity
@Import(SecurityProblemSupport.class)
public class SecurityConfiguration {

    private final JHipsterProperties jHipsterProperties;
    private final TokenProvider tokenProvider;
    private final CorsFilter corsFilter;
    private final SecurityProblemSupport problemSupport;

    public SecurityConfiguration(
        TokenProvider tokenProvider,
        CorsFilter corsFilter,
        JHipsterProperties jHipsterProperties,
        SecurityProblemSupport problemSupport
    ) {
        this.tokenProvider = tokenProvider;
        this.corsFilter = corsFilter;
        this.problemSupport = problemSupport;
        this.jHipsterProperties = jHipsterProperties;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:9000", "http://localhost:4200", "http://localhost:8100"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Link", "X-Total-Count"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // @formatter:off
        http
            .cors().configurationSource(corsConfigurationSource())
            .and()
            .csrf()
            .disable() // CSRF should be disabled for stateless JWT authentication
            .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling()
                .authenticationEntryPoint(problemSupport)
                .accessDeniedHandler(problemSupport)
        .and()
            .headers()
                .httpStrictTransportSecurity().maxAgeInSeconds(31536000).includeSubDomains(true)
            .and()
                .contentSecurityPolicy(jHipsterProperties.getSecurity().getContentSecurityPolicy())
            .and()
                .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)
            .and()
                .xssProtection().block(true)
            .and()
                .permissionsPolicy().policy("camera=(), fullscreen=(self), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), midi=(), payment=(), sync-xhr=()")
            .and()
                .frameOptions().sameOrigin() // Allow same-origin frames for BPMN iframe
        .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // No session is stored on the server
        .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .antMatchers("/app/**/*.{js,html}").permitAll()
            .antMatchers("/i18n/**").permitAll()
            .antMatchers("/content/**").permitAll()
            .antMatchers("/swagger-ui/**").hasAuthority(AuthoritiesConstants.ADMIN) // Admin access to Swagger UI
            .antMatchers("/test/**").permitAll()
            .antMatchers("/api/authenticate").permitAll() // Public authentication endpoint
            .antMatchers("/api/register").permitAll()
            .antMatchers("/api/activate").permitAll()
            .antMatchers("/api/account/reset-password/init").permitAll()
            .antMatchers("/api/account/reset-password/finish").permitAll()
            .antMatchers("/api/captcha-endpoint").permitAll() // Public CAPTCHA endpoint
            .antMatchers("/api/captcha-image/**").permitAll() // Public CAPTCHA image endpoint
            .antMatchers("/api/captcha-validate").permitAll() // Public CAPTCHA validation endpoint
            .antMatchers("/api/admin/**").hasAuthority(AuthoritiesConstants.ADMIN) // Admin API access
            .antMatchers("/api/**").authenticated() // All other API endpoints require authentication
            .antMatchers("/management/health").hasAuthority(AuthoritiesConstants.ADMIN) // Admin access to health endpoint
            .antMatchers("/management/health/**").hasAuthority(AuthoritiesConstants.ADMIN) // Admin access to health details
            .antMatchers("/management/info").permitAll() // Public info endpoint
            .antMatchers("/management/prometheus").denyAll() // Deny access to Prometheus metrics
            .antMatchers("/management/threaddump").denyAll() // Deny access to thread dump
            .antMatchers("/management/jhimetrics").denyAll() // Deny access to JHipster metrics
            .antMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN) // Admin access to all other management endpoints
        .and()
            .httpBasic() // Basic authentication
        .and()
            .apply(securityConfigurerAdapter());
        return http.build();
        // @formatter:on
    }

    private JWTConfigurer securityConfigurerAdapter() {
        return new JWTConfigurer(tokenProvider);
    }
}
