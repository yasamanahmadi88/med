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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class CaptchaValidationService {

    private static final Logger log = LoggerFactory.getLogger(CaptchaValidationService.class);

    private final CaptchaProperties props;

    private final LocalCaptchaService localCaptchaService;

    private final RestTemplate restTemplate = new RestTemplate();

    public CaptchaValidationService(CaptchaProperties props, LocalCaptchaService localCaptchaService) {
        this.props = props;
        this.localCaptchaService = localCaptchaService;
    }

    public void validate(String captchaId, String captchaToken, String remoteIp) {
        if (!props.isEnabled() || props.isDevBypass()) {
            log.trace(
                "CAPTCHA validation bypassed. enabled={}, devBypass={}",
                props.isEnabled(),
                props.isDevBypass()
            );
            return;
        }

        if (captchaToken == null || captchaToken.isBlank()) {
            throw new InvalidCaptchaException("Invalid authentication request");
        }

        if ("local".equalsIgnoreCase(props.getProvider())) {
            validateLocalCaptcha(captchaId, captchaToken);
            return;
        }

        validateRemoteCaptcha(captchaToken, remoteIp);
    }

    private void validateLocalCaptcha(String captchaId, String captchaToken) {
        if (captchaId == null || captchaId.isBlank()) {
            throw new InvalidCaptchaException("Invalid authentication request");
        }

        boolean valid = localCaptchaService.verifyAndConsume(
            captchaId.trim(),
            captchaToken.trim()
        );

        if (!valid) {
            throw new InvalidCaptchaException("Invalid authentication request");
        }
    }

    private void validateRemoteCaptcha(String captchaToken, String remoteIp) {
        if (props.getVerifyUrl() == null || props.getVerifyUrl().isBlank()) {
            log.warn("CAPTCHA verify URL is not configured.");
            throw new InvalidCaptchaException("Invalid authentication request");
        }

        if (props.getSecretKey() == null || props.getSecretKey().isBlank()) {
            log.warn("CAPTCHA secret key is not configured.");
            throw new InvalidCaptchaException("Invalid authentication request");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("secret", props.getSecretKey());
        form.add("response", captchaToken);

        if (remoteIp != null && !remoteIp.isBlank()) {
            form.add("remoteip", remoteIp);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        VerifyResponse response;

        try {
            response = restTemplate.postForObject(
                props.getVerifyUrl(),
                new HttpEntity<>(form, headers),
                VerifyResponse.class
            );
        } catch (RestClientException ex) {
            log.debug("CAPTCHA remote verification failed.");
            throw new InvalidCaptchaException("Invalid authentication request");
        }

        if (response == null || !Boolean.TRUE.equals(response.success)) {
            log.debug("CAPTCHA verification failed.");
            throw new InvalidCaptchaException("Invalid authentication request");
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class VerifyResponse {

        @JsonProperty("success")
        private Boolean success;

        @JsonProperty("score")
        private Double score;

        @JsonProperty("error-codes")
        private String[] errorCodes;

        @Override
        public String toString() {
            return "VerifyResponse{success=" + success + ", score=" + score + '}';
        }
    }
}
