package com.behsa.medportal.config;

import com.behsa.medportal.security.captcha.CaptchaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Simple glue so Spring knows about our {@link CaptchaProperties}.
 */
@Configuration
@EnableConfigurationProperties(CaptchaProperties.class)
public class CaptchaConfiguration {
    // Empty – we only need the annotation.
}
