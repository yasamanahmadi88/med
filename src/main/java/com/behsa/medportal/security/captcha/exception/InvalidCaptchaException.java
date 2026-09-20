package com.behsa.medportal.security.captcha.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Thrown when the captcha token fails remote validation.
 * Spring’s {@link org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler}
 * (or JHipster’s ExceptionTranslator) converts this into a 400 JSON response.
 */
public class InvalidCaptchaException extends AuthenticationException {

    public InvalidCaptchaException(String msg) {
        super(msg);
    }
}
