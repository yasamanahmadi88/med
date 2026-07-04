package com.behsa.medportal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Enables @PreAuthorize / @PostAuthorize and @Secured method security.
 */
@Configuration
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class MethodSecurityConfiguration {}
