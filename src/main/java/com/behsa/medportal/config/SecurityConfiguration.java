package com.behsa.medportal.config;

import com.behsa.medportal.security.AuthoritiesConstants;
import com.behsa.medportal.security.SecurityCache;
import com.behsa.medportal.security.jwt.JWTConfigurer;
import com.behsa.medportal.security.jwt.TokenProvider;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tech.jhipster.config.JHipsterProperties;

import java.util.Arrays;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    /**
     * Backend / infrastructure path roots that must never be served the Angular shell.
     * Kept in sync with {@code ClientForwardController#isBackendOrInfrastructurePath}.
     */
    private static final Set<String> BACKEND_PATH_ROOTS = Set.of(
        "/api",
        "/management",
        "/v3",
        "/swagger-ui",
        "/swagger-resources",
        "/api-docs"
    );

    /** Matches the deep links that Spring MVC forwards to the Angular shell. See {@link #isClientRoute}. */
    private static final RequestMatcher CLIENT_ROUTE = SecurityConfiguration::isClientRoute;

    private final JHipsterProperties jHipsterProperties;
    private final TokenProvider tokenProvider;
    private final SecurityCache securityCache;

    public SecurityConfiguration(
        TokenProvider tokenProvider,
        JHipsterProperties jHipsterProperties,
        SecurityCache securityCache
    ) {
        this.tokenProvider = tokenProvider;
        this.jHipsterProperties = jHipsterProperties;
        this.securityCache = securityCache;
    }

    /**
     * Whether the request is an Angular client-side route that Spring MVC forwards to {@code index.html}.
     *
     * <p>Mirrors {@code ClientForwardController#forward}: a GET (or HEAD) whose last path segment carries
     * no file extension, and that does not target a backend/infrastructure root, is a deep link such as
     * {@code /login} or {@code /admin/user-management}. Those requests must stay anonymous, otherwise
     * refreshing a deep link — and reaching the login page at all — would be rejected by the
     * {@code denyAll()} tail. Static files always carry an extension and are covered by the SPA
     * allow-list instead.</p>
     */
    private static boolean isClientRoute(HttpServletRequest request) {
        String method = request.getMethod();
        if (!HttpMethod.GET.matches(method) && !HttpMethod.HEAD.matches(method)) {
            return false;
        }

        String uri = request.getRequestURI();
        if (uri == null || !uri.startsWith("/")) {
            return false;
        }

        for (String root : BACKEND_PATH_ROOTS) {
            if (uri.equals(root) || uri.startsWith(root + "/")) {
                return false;
            }
        }

        // No extension in the last segment => client-side route, not a static file.
        return uri.lastIndexOf('.') <= uri.lastIndexOf('/');
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
                    // Spring Security authorizes ERROR and ASYNC dispatches too; denying them would turn
                    // every error page and every async re-dispatch into an empty 403.
                    .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.ASYNC).permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    // PathPatternParser forbids ** in the middle; permit the whole /app tree.
                    .requestMatchers("/app/**").permitAll()
                    .requestMatchers("/i18n/**").permitAll()
                    .requestMatchers("/content/**").permitAll()
                    .requestMatchers("/api/authenticate").permitAll()
                    .requestMatchers("/api/auth/**").permitAll()
                    // The captcha guards the login form, so its whole flow runs before authentication.
                    .requestMatchers("/api/captcha-endpoint").permitAll()
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
                    // Public build/version banner. Previously public only by falling through to the
                    // permit-all tail; spelled out here so the deny-all tail does not change its behaviour.
                    .requestMatchers(HttpMethod.GET, "/version", "/version/info").permitAll()
                    // SPA shell + hashed Angular assets (API/management already matched above).
                    // Everything the Angular build drops into target/classes/static must appear here.
                    .requestMatchers(
                        "/",
                        "/index.html",
                        "/*.js",
                        "/*.css",
                        "/*.map",
                        "/*.ico",
                        "/*.png",
                        "/*.svg",
                        "/*.webp",
                        "/*.gif",
                        "/*.jpg",
                        "/*.jpeg",
                        "/*.woff",
                        "/*.woff2",
                        "/*.ttf",
                        "/*.eot",
                        "/*.webmanifest",
                        "/manifest.webapp",
                        "/robots.txt",
                        "/3rdpartylicenses.txt",
                        "/ngsw.json",
                        "/media/**",
                        "/assets/**"
                    ).permitAll()
                    // Angular deep links (no hash routing): forwarded to index.html by ClientForwardController.
                    .requestMatchers(CLIENT_ROUTE).permitAll()
                    // Fail closed: anything not matched above is denied rather than silently public.
                    // Static SPA files belong in the allow-list directly above; client-side routes are
                    // covered by CLIENT_ROUTE; new endpoints must declare their own rule.
                    .anyRequest().denyAll()
            )
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable());

        http.with(new JWTConfigurer(tokenProvider, securityCache), customizer -> {});

        return http.build();
    }
}
