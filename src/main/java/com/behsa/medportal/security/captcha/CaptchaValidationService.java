// src/main/java/com/behsa/medportal/security/captcha/CaptchaValidationService.java
package com.behsa.medportal.security.captcha;

import com.behsa.medportal.security.captcha.exception.InvalidCaptchaException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;

@Service
public class CaptchaValidationService {

    private static final Logger log = LoggerFactory.getLogger(CaptchaValidationService.class);

    private final CaptchaProperties props;
    private final LocalCaptchaService local;
    private final RestTemplate restTemplate = new RestTemplate();

    public CaptchaValidationService(CaptchaProperties props, LocalCaptchaService local) {
        this.props = props;
        this.local = local;
    }

    /**
     * Validates the captcha token. In 'local' mode, token is the user's typed code and
     * we verify against the last issued CAPTCHA_ID stored in the HttpSession.
     */
    public void validate(String token, String remoteIp) {
        if (!props.isEnabled() || props.isDevBypass()) {
            log.trace("CAPTCHA validation bypassed (enabled={}, devBypass={})",
                props.isEnabled(), props.isDevBypass());
            return;
        }

        if (token == null || token.isBlank()) {
            throw new InvalidCaptchaException("Missing captcha token");
        }

        // ── LOCAL: compare with session-stored id & consume ───────────────────
        if ("local".equalsIgnoreCase(props.getProvider())) {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpSession session = (attrs != null) ? attrs.getRequest().getSession(false) : null;
            String id = (session != null) ? (String) session.getAttribute("CAPTCHA_ID") : null;
            if (id == null) {
                throw new InvalidCaptchaException("Captcha session not found");
            }
            boolean ok = local.verifyAndConsume(id, token);
            // Clear the session slot either way (prevent reuse)
            try { session.removeAttribute("CAPTCHA_ID"); } catch (Exception ignore) {}
            if (!ok) throw new InvalidCaptchaException("Invalid or expired captcha");
            return;
        }

        // ── REMOTE providers (turnstile/recaptcha/hcaptcha) ──────────────────
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("secret", props.getSecretKey());
        form.add("response", token);
        if (remoteIp != null) {
            form.add("remoteip", remoteIp);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        VerifyResponse response = restTemplate.postForObject(
            props.getVerifyUrl(),
            new HttpEntity<>(form, headers),
            VerifyResponse.class
        );

        if (response == null || !Boolean.TRUE.equals(response.success)) {
            log.warn("CAPTCHA verification failed: {}", response);
            throw new InvalidCaptchaException("Invalid captcha");
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class VerifyResponse {
        @JsonProperty("success") Boolean success;
        @JsonProperty("score")   Double score;
        @JsonProperty("error-codes") String[] errorCodes;

        @Override public String toString() {
            return "VerifyResponse{success=" + success + ", score=" + score + '}';
        }
    }
}
