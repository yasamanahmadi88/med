package com.behsa.medportal.config;

import com.behsa.medportal.security.AuthoritiesConstants;
import com.behsa.medportal.security.SecurityCache;
import com.behsa.medportal.security.jwt.JWTConfigurer;
import com.behsa.medportal.security.jwt.TokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;
import tech.jhipster.config.JHipsterProperties;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@Import(SecurityProblemSupport.class)
public class SecurityConfiguration {

    private final JHipsterProperties jHipsterProperties;
    private final TokenProvider tokenProvider;
    private final CorsFilter corsFilter;
    private final SecurityProblemSupport problemSupport;
    private final SecurityCache securityCache;

    public SecurityConfiguration(
        TokenProvider tokenProvider,
        CorsFilter corsFilter,
        JHipsterProperties jHipsterProperties,
        SecurityProblemSupport problemSupport,
        SecurityCache securityCache
    ) {
        this.tokenProvider = tokenProvider;
        this.corsFilter = corsFilter;
        this.problemSupport = problemSupport;
        this.jHipsterProperties = jHipsterProperties;
        this.securityCache = securityCache;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:9000",
            "http://localhost:4200",
            "http://localhost:8100"
        ));

        configuration.setAllowedMethods(Arrays.asList(
            "GET",
            "POST",
            "PUT",
            "PATCH",
            "DELETE",
            "OPTIONS"
        ));

        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Cache-Control",
            "Content-Type",
            "X-Requested-With"
        ));

        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Link",
            "X-Total-Count"
        ));

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(
        org.springframework.security.config.annotation.web.builders.HttpSecurity http
    ) throws Exception {
        http
            .cors()
            .configurationSource(corsConfigurationSource())
            .and()
            .csrf()
            .disable()
            .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling()
            .authenticationEntryPoint(problemSupport)
            .accessDeniedHandler(problemSupport)
            .and()
            .headers()
            .httpStrictTransportSecurity()
            .maxAgeInSeconds(31536000)
            .includeSubDomains(true)
            .and()
            .contentSecurityPolicy(jHipsterProperties.getSecurity().getContentSecurityPolicy())
            .and()
            .referrerPolicy(
                org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER
            )
            .and()
            .xssProtection()
            .block(true)
            .and()
            .permissionsPolicy()
            .policy(
                "camera=(), fullscreen=(self), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), midi=(), payment=(), sync-xhr=()"
            )
            .and()
            .frameOptions()
            .sameOrigin()
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()

            // Preflight
            .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()

            // Frontend static assets
            .antMatchers("/app/**/*.{js,html}").permitAll()
            .antMatchers("/i18n/**").permitAll()

            // If bpmnjs content must be protected, this rule must come before /content/**
            .antMatchers("/content/bpmnjs/**").authenticated()
            .antMatchers("/content/**").permitAll()

            .antMatchers("/test/**").permitAll()

            // Authentication and CAPTCHA
            .antMatchers("/api/authenticate").permitAll()
            .antMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()
            .antMatchers(HttpMethod.POST, "/api/captcha-endpoint").permitAll()
            .antMatchers(HttpMethod.GET, "/api/captcha.png").permitAll()

            // Disable public self-registration
            .antMatchers(HttpMethod.POST, "/api/register").denyAll()

            // Public account recovery endpoints
            .antMatchers(HttpMethod.POST,
                "/api/account/reset-password/init",
                "/api/account/reset-password/finish"
            ).permitAll()

            .antMatchers(HttpMethod.GET, "/api/activate").permitAll()

            // Swagger / API docs: admin only
            .antMatchers(
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/v3/api-docs",
                "/v3/api-docs/**",
                "/swagger-resources/**",
                "/api-docs",
                "/api-docs/**"
            ).hasAuthority(AuthoritiesConstants.ADMIN)

            // Admin-only/security-management APIs
            .antMatchers("/api/admin/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers(HttpMethod.GET, "/api/authorities").hasAuthority(AuthoritiesConstants.ADMIN)

            .antMatchers("/api/resources/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/api/resource-authorities/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/api/med-authorities/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/api/custom-audit-events/**").hasAuthority(AuthoritiesConstants.ADMIN)

            // Normal APIs: authenticated first, then @Secured + CustomAccessDecisionManager decides VIEW/CREATE/EDIT/DELETE
            .antMatchers("/api/**").authenticated()

            // Management endpoints
            .antMatchers("/management/health", "/management/health/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/management/info").hasAuthority(AuthoritiesConstants.ADMIN)

            // Highly sensitive management endpoints: blocked for everyone
            .antMatchers(
                "/management/prometheus",
                "/management/threaddump",
                "/management/jhimetrics",
                "/management/env",
                "/management/env/**",
                "/management/configprops",
                "/management/configprops/**",
                "/management/loggers",
                "/management/loggers/**",
                "/management/metrics",
                "/management/metrics/**"
            ).denyAll()

            .antMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN)

            .and()
            .httpBasic()
            .disable()
            .formLogin()
            .disable();

        http.apply(new JWTConfigurer(tokenProvider, securityCache));

        return http.build();
    }
}
