package com.behsa.medportal.config;

import com.behsa.medportal.security.AuthoritiesConstants;
import com.behsa.medportal.security.SecurityCache;
import com.behsa.medportal.security.jwt.JWTConfigurer;
import com.behsa.medportal.security.jwt.TokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import tech.jhipster.config.JHipsterProperties;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final JHipsterProperties jHipsterProperties;
    private final TokenProvider tokenProvider;
    private final CorsFilter corsFilter;
    private final SecurityCache securityCache;

    public SecurityConfiguration(
        TokenProvider tokenProvider,
        CorsFilter corsFilter,
        JHipsterProperties jHipsterProperties,
        SecurityCache securityCache
    ) {
        this.tokenProvider = tokenProvider;
        this.corsFilter = corsFilter;
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
            "http://localhost:8100",
            "http://localhost:9060"
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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(exceptions ->
                exceptions
                    .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.sendError(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase());
                    })
            )
            .headers(headers ->
                headers
                    .httpStrictTransportSecurity(hsts -> hsts.maxAgeInSeconds(31536000).includeSubDomains(true))
                    .contentSecurityPolicy(csp -> csp.policyDirectives(jHipsterProperties.getSecurity().getContentSecurityPolicy()))
                    .referrerPolicy(referrer ->
                        referrer.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)
                    )
                    .permissionsPolicyHeader(permissions ->
                        permissions.policy(
                            "camera=(), fullscreen=(self), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), midi=(), payment=(), sync-xhr=()"
                        )
                    )
                    .frameOptions(frame -> frame.sameOrigin())
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz ->
                authz
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    // PathPatternParser forbids ** in the middle; permit the whole /app tree.
                    .requestMatchers("/app/**").permitAll()
                    .requestMatchers("/i18n/**").permitAll()
                    .requestMatchers("/content/bpmnjs/**").authenticated()
                    .requestMatchers("/content/**").permitAll()
                    .requestMatchers("/test/**").permitAll()
                    .requestMatchers("/api/authenticate").permitAll()
                    .requestMatchers("/api/auth/**").permitAll()
                    // The captcha guards the login form, so its whole flow runs before authentication.
                    .requestMatchers("/api/captcha-endpoint", "/api/captcha-validate").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/captcha.png").permitAll()
                    // Public self-registration is disabled for this deployment.
                    .requestMatchers("/api/register").denyAll()
                    .requestMatchers("/api/activate").permitAll()
                    .requestMatchers("/api/account/reset-password/**").permitAll()
                    .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/api-docs",
                        "/api-docs/**"
                    ).hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers("/api/admin/**").hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers(HttpMethod.GET, "/api/authorities").hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers("/api/resources/**").hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers("/api/resource-authorities/**").hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers("/api/med-authorities/**").hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers("/api/custom-audit-events/**").hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers("/api/**").authenticated()
                    .requestMatchers("/management/health", "/management/health/**").hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers("/management/info").hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers(
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
                    .requestMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN)
                    // SPA shell + hashed Angular assets (API/management already matched above).
                    .requestMatchers(
                        "/",
                        "/index.html",
                        "/*.js",
                        "/*.css",
                        "/*.map",
                        "/*.ico",
                        "/*.png",
                        "/*.svg",
                        "/*.woff",
                        "/*.woff2",
                        "/*.ttf",
                        "/media/**",
                        "/assets/**"
                    ).permitAll()
                    .anyRequest().permitAll()
            )
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable());

        http.with(new JWTConfigurer(tokenProvider, securityCache), customizer -> {});

        return http.build();
    }
}
