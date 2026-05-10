package com.behsa.medportal.config;

import com.behsa.medportal.security.AuthoritiesConstants;
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

@Configuration                   // <-- add
@EnableWebSecurity               // <-- add
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
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:9000","http://localhost:4200","http://localhost:8100"));
        configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization","Cache-Control","Content-Type","X-Requested-With"));
        configuration.setExposedHeaders(Arrays.asList("Authorization","Link","X-Total-Count"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
        http
            .cors().configurationSource(corsConfigurationSource())
            .and()
            .csrf().disable()
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
            .referrerPolicy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)
            .and()
            .xssProtection().block(true)
            .and()
            .permissionsPolicy().policy("camera=(), fullscreen=(self), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), midi=(), payment=(), sync-xhr=()")
            .and()
            .frameOptions().sameOrigin()
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .antMatchers("/app/**/*.{js,html}").permitAll()
            .antMatchers("/i18n/**").permitAll()
            .antMatchers("/content/**").permitAll()
            .antMatchers("/swagger-ui/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/test/**").permitAll()

            // authenticate endpoint has both GET and POST in JHipster tests
            .antMatchers("/api/authenticate").permitAll()

            // public account endpoints
            .antMatchers(HttpMethod.POST,
                "/api/register",
                "/api/account/reset-password/init",
                "/api/account/reset-password/finish"
            ).permitAll()
            .antMatchers(HttpMethod.GET, "/api/activate").permitAll()
            // captcha endpoints
            .antMatchers(HttpMethod.POST, "/api/captcha-endpoint", "/api/captcha-validate").permitAll()
            .antMatchers(HttpMethod.GET,  "/api/captcha.png").permitAll()
            // admin & other APIs
            .antMatchers("/api/admin/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/api/**").authenticated()
            .antMatchers("/management/health", "/management/health/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/management/info").permitAll()
            .antMatchers("/management/prometheus",
                "/management/threaddump",
                "/management/jhimetrics").denyAll()
            .antMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .and()
            .httpBasic()
            .and()
            .apply(new JWTConfigurer(tokenProvider));

        return http.build();
    }
}
