package com.behsa.medportal.config;

import com.behsa.medportal.service.ResourceAuthorityQueryService;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

/**
 * Custom method security configuration
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, proxyTargetClass = true, mode = AdviceMode.PROXY)
public class MethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {

    private final ResourceAuthorityQueryService resourceAuthorityQueryService;

    public MethodSecurityConfiguration(ResourceAuthorityQueryService resourceAuthorityQueryService) {
        this.resourceAuthorityQueryService = resourceAuthorityQueryService;
    }

    public AccessDecisionManager accessDecisionManager() {
        CustomAccessDecisionManager accessDecisionManager = new CustomAccessDecisionManager(resourceAuthorityQueryService);
        return accessDecisionManager;
    }
}
