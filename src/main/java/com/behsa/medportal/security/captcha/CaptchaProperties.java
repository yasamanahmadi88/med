package com.behsa.medportal.security.captcha;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "captcha")
public class CaptchaProperties {

    /** turnstile | recaptcha | hcaptcha | other */
    private String provider = "turnstile";

    /** Shared secret used when calling the provider verify endpoint. */
    private String secretKey;

    /** Full HTTPS URL of the provider’s verify API. */
    private String verifyUrl;

    /** Switch CAPTCHA on/off globally (prod ≠ dev). */
    private boolean enabled = true;

    /** Allows bypass in DEV profile without calling provider. */
    private boolean devBypass = false;

    // ─────────────────── getters / setters ───────────────────

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getVerifyUrl() {
        return verifyUrl;
    }

    public void setVerifyUrl(String verifyUrl) {
        this.verifyUrl = verifyUrl;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isDevBypass() {
        return devBypass;
    }

    public void setDevBypass(boolean devBypass) {
        this.devBypass = devBypass;
    }
}
