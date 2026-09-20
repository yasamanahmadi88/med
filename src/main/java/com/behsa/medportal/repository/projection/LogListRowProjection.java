package com.behsa.medportal.repository.projection;

import java.time.LocalDateTime;

/**
 * Repository-level projection for log list rows.
 *
 * This class belongs to the persistence layer, so repositories can safely use it
 * without depending on service DTOs.
 */
public class LogListRowProjection {

    private final Long id;
    private final String msgType;
    private final String correlationId;
    private final String referenceType;
    private final String reference;
    private final String moduleSource;
    private final String moduleDestination;
    private final String properties;
    private final String reqMessage;
    private final String resMessage;
    private final String error;
    private final String errorDetails;
    private final LocalDateTime initialDate;
    private final LocalDateTime createDate;
    private final String logType;

    public LogListRowProjection(
        Long id,
        String msgType,
        String correlationId,
        String referenceType,
        String reference,
        String moduleSource,
        String moduleDestination,
        String properties,
        String reqMessage,
        String resMessage,
        String error,
        String errorDetails,
        LocalDateTime initialDate,
        LocalDateTime createDate,
        String logType
    ) {
        this.id = id;
        this.msgType = msgType;
        this.correlationId = correlationId;
        this.referenceType = referenceType;
        this.reference = reference;
        this.moduleSource = moduleSource;
        this.moduleDestination = moduleDestination;
        this.properties = properties;
        this.reqMessage = reqMessage;
        this.resMessage = resMessage;
        this.error = error;
        this.errorDetails = errorDetails;
        this.initialDate = initialDate;
        this.createDate = createDate;
        this.logType = logType;
    }

    public Long getId() {
        return id;
    }

    public String getMsgType() {
        return msgType;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public String getReference() {
        return reference;
    }

    public String getModuleSource() {
        return moduleSource;
    }

    public String getModuleDestination() {
        return moduleDestination;
    }

    public String getProperties() {
        return properties;
    }

    public String getReqMessage() {
        return reqMessage;
    }

    public String getResMessage() {
        return resMessage;
    }

    public String getError() {
        return error;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public LocalDateTime getInitialDate() {
        return initialDate;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public String getLogType() {
        return logType;
    }
}
