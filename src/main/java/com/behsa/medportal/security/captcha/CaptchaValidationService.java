package com.behsa.medportal.security.captcha;

import com.behsa.medportal.security.captcha.exception.InvalidCaptchaException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class CaptchaValidationService {

    private static final Logger log = LoggerFactory.getLogger(CaptchaValidationService.class);

    private final CaptchaProperties props;
    private final RestTemplate restTemplate = new RestTemplate();

    public CaptchaValidationService(CaptchaProperties props) {
        this.props = props;
    }

    /**
     * Verifies that the token supplied by the front-end is accepted by the provider.
     *
     * @param token    the opaque string returned by Turnstile / reCAPTCHA / hCaptcha
     * @param remoteIp (optional) IP address of caller – passed for provider logging
     * @throws InvalidCaptchaException if validation fails
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
        // For score-based providers (reCAPTCHA v3) you may add a score threshold here.
    }

    // ─────────────────── DTO for provider JSON ───────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class VerifyResponse {

        @JsonProperty("success")
        Boolean success;

        @JsonProperty("score")
        Double score;

        @JsonProperty("error-codes")
        String[] errorCodes;

        @Override
        public String toString() {
            return "VerifyResponse{" +
                "success=" + success +
                ", score=" + score +
                '}';
        }
    }
}
