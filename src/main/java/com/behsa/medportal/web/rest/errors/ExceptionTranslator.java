package com.behsa.medportal.web.rest.errors;

import com.behsa.medportal.service.UsernameAlreadyUsedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.zalando.problem.*;
import org.zalando.problem.spring.web.advice.ProblemHandling;
import org.zalando.problem.spring.web.advice.security.SecurityAdviceTrait;
import org.zalando.problem.violations.ConstraintViolationProblem;
import tech.jhipster.web.util.HeaderUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller advice to translate the server side exceptions to client-friendly json structures.
 * The error response follows RFC7807 - Problem Details for HTTP APIs (https://tools.ietf.org/html/rfc7807).
 */
@ControllerAdvice
public class ExceptionTranslator implements ProblemHandling, SecurityAdviceTrait {

    private static final String FIELD_ERRORS_KEY = "fieldErrors";
    private static final String MESSAGE_KEY = "message";
    private static final String PATH_KEY = "path";
    private static final String VIOLATIONS_KEY = "violations";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final Environment env;

    public ExceptionTranslator(Environment env) {
        this.env = env;
    }

    /**
     * Post-process the Problem payload to add the message key for the front-end if needed.
     */
    @Override
    public ResponseEntity<Problem> process(@Nullable ResponseEntity<Problem> entity, NativeWebRequest request) {
        if (entity == null) {
            return null;
        }
        Problem problem = entity.getBody();
        if (!(problem instanceof ConstraintViolationProblem || problem instanceof DefaultProblem)) {
            return entity;
        }

        HttpServletRequest nativeRequest = request.getNativeRequest(HttpServletRequest.class);
        String requestUri = nativeRequest != null ? nativeRequest.getRequestURI() : StringUtils.EMPTY;
        Map<String, Object> payload = createProblemPayload(problem, requestUri);
        return createProblemResponse(payload, entity);
    }

    private Map<String, Object> createProblemPayload(Problem problem, String requestUri) {
        Map<String, Object> payload = new LinkedHashMap<>();
        URI type = Problem.DEFAULT_TYPE.equals(problem.getType()) ? ErrorConstants.DEFAULT_TYPE : problem.getType();

        payload.put("type", type.toString());
        if (problem.getTitle() != null) {
            payload.put("title", problem.getTitle());
        }
        if (problem.getStatus() != null) {
            payload.put("status", problem.getStatus().getStatusCode());
        }
        if (problem.getDetail() != null) {
            payload.put("detail", problem.getDetail());
        }
        if (problem.getInstance() != null) {
            payload.put("instance", problem.getInstance().toString());
        }
        payload.put(PATH_KEY, requestUri);

        if (problem instanceof ConstraintViolationProblem constraintViolationProblem) {
            payload.put(VIOLATIONS_KEY, constraintViolationProblem.getViolations());
            payload.put(MESSAGE_KEY, ErrorConstants.ERR_VALIDATION);
        } else {
            payload.putAll(problem.getParameters());
            if (!payload.containsKey(MESSAGE_KEY) && problem.getStatus() != null) {
                payload.put(MESSAGE_KEY, "error.http." + problem.getStatus().getStatusCode());
            }
        }

        return payload;
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Problem> createProblemResponse(Map<String, Object> payload, ResponseEntity<Problem> entity) {
        return (ResponseEntity<Problem>) (ResponseEntity<?>) new ResponseEntity<>(payload, entity.getHeaders(), entity.getStatusCode());
    }

    @Override
    public ResponseEntity<Problem> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, @Nonnull NativeWebRequest request) {
        BindingResult result = ex.getBindingResult();
        List<FieldErrorVM> fieldErrors = result
            .getFieldErrors()
            .stream()
            .map(f ->
                new FieldErrorVM(
                    f.getObjectName().replaceFirst("DTO$", ""),
                    f.getField(),
                    StringUtils.isNotBlank(f.getDefaultMessage()) ? f.getDefaultMessage() : f.getCode()
                )
            )
            .collect(Collectors.toList());

        Problem problem = Problem
            .builder()
            .withType(ErrorConstants.CONSTRAINT_VIOLATION_TYPE)
            .withTitle("Method argument not valid")
            .withStatus(defaultConstraintViolationStatus())
            .with(MESSAGE_KEY, ErrorConstants.ERR_VALIDATION)
            .with(FIELD_ERRORS_KEY, fieldErrors)
            .build();
        return create(ex, problem, request);
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleEmailAlreadyUsedException(
        com.behsa.medportal.service.EmailAlreadyUsedException ex,
        NativeWebRequest request
    ) {
        EmailAlreadyUsedException problem = new EmailAlreadyUsedException();
        return create(
            problem,
            request,
            HeaderUtil.createFailureAlert(applicationName, true, problem.getEntityName(), problem.getErrorKey(), problem.getMessage())
        );
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleUsernameAlreadyUsedException(
        UsernameAlreadyUsedException ex,
        NativeWebRequest request
    ) {
        LoginAlreadyUsedException problem = new LoginAlreadyUsedException();
        return create(
            problem,
            request,
            HeaderUtil.createFailureAlert(applicationName, true, problem.getEntityName(), problem.getErrorKey(), problem.getMessage())
        );
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleInvalidPasswordException(
        com.behsa.medportal.service.InvalidPasswordException ex,
        NativeWebRequest request
    ) {
        return create(new InvalidPasswordException(), request);
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleBadRequestAlertException(BadRequestAlertException ex, NativeWebRequest request) {
        return create(
            ex,
            request,
            HeaderUtil.createFailureAlert(applicationName, true, ex.getEntityName(), ex.getErrorKey(), ex.getMessage())
        );
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleConcurrencyFailure(ConcurrencyFailureException ex, NativeWebRequest request) {
        Problem problem = Problem.builder().withStatus(Status.CONFLICT).with(MESSAGE_KEY, ErrorConstants.ERR_CONCURRENCY_FAILURE).build();
        return create(ex, problem, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Problem> handleMethodArgumentTypeMismatch(
        MethodArgumentTypeMismatchException ex,
        NativeWebRequest request
    ) {
        Problem problem = Problem
            .builder()
            .withType(ErrorConstants.CONSTRAINT_VIOLATION_TYPE)
            .withTitle("Bad Request")
            .withStatus(Status.BAD_REQUEST)
            .with(MESSAGE_KEY, ErrorConstants.ERR_VALIDATION)
            .build();

        return create(ex, problem, request);
    }

    @ExceptionHandler(HttpMessageConversionException.class)
    public ResponseEntity<Problem> handleHttpMessageConversionException(
        HttpMessageConversionException ex,
        NativeWebRequest request
    ) {
        Problem problem = Problem
            .builder()
            .withType(ErrorConstants.DEFAULT_TYPE)
            .withTitle("Bad Request")
            .withStatus(Status.BAD_REQUEST)
            .with(MESSAGE_KEY, "error.http.400")
            .build();

        return create(ex, problem, request);
    }

    @Override
    public ProblemBuilder prepare(final Throwable throwable, final StatusType status, final URI type) {
        if (shouldHideExceptionDetail(throwable, status)) {
            return Problem
                .builder()
                .withType(type)
                .withTitle(status.getReasonPhrase())
                .withStatus(status);
        }

        return Problem
            .builder()
            .withType(type)
            .withTitle(status.getReasonPhrase())
            .withStatus(status)
            .withDetail(getDetail(throwable));
    }

    private String getDetail(Throwable throwable) {
        if (throwable instanceof HttpRequestMethodNotSupportedException ex) {
            return "Request method '" + ex.getMethod() + "' not supported";
        }

        return throwable.getMessage();
    }

    private boolean shouldHideExceptionDetail(Throwable throwable, StatusType status) {
        if (status == null) {
            return true;
        }

        if (status.getStatusCode() >= 500) {
            return true;
        }

        if (throwable instanceof HttpMessageConversionException) {
            return true;
        }

        if (throwable instanceof DataAccessException) {
            return true;
        }

        if (throwable instanceof MethodArgumentTypeMismatchException) {
            return true;
        }

        return containsPackageName(throwable.getMessage());
    }


    private boolean containsPackageName(String message) {
        if (message == null) {
            return false;
        }

        return StringUtils.containsAny(
            message,
            "org.",
            "java.",
            "javax.",
            "jakarta.",
            "net.",
            "com.",
            "io.",
            "de.",
            "oracle.",
            "hibernate",
            "SQLException",
            "Exception",
            "C:\\",
            "E:\\",
            "com.behsa.medportal"
        );
    }



}
