package com.behsa.medportal.service.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import jakarta.validation.constraints.*;

/**
 * A DTO for log list rows (summary projection).
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class LogListRowDTO implements Serializable {


    private Long id;

    @Size(max = 100)
    private String msgType;

    @Size(max = 100)
    private String correlationId;

    @Size(max = 100)
    private String referenceType;

    @Size(max = 100)
    private String reference;

    @Size(max = 50)
    private String moduleSource;

    @Size(max = 50)
    private String moduleDestination;

    // CLOBs exposed as String in DTO (no size constraint)
    private String properties;

    private String reqMessage;

    private String resMessage;

    @Size(max = 200)
    private String error;

    @Size(max = 4000)
    private String errorDetails;

    private LocalDateTime initialDate;

    private LocalDateTime createDate;

    @Size(max = 100)
    private String logType;

    /** No-args constructor (useful for Jackson, etc.). */
    public LogListRowDTO() {}

    public LogListRowDTO(Long id, String msgType, String correlationId, String referenceType, String reference, String moduleSource, String moduleDestination, String properties, String reqMessage, String resMessage, String error, String errorDetails, LocalDateTime initialDate, LocalDateTime createDate, String logType) {
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



    /** Keeps JPQL constructor projection compatible. */


    // --- getters/setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMsgType() { return msgType; }
    public void setMsgType(String msgType) { this.msgType = msgType; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getModuleSource() { return moduleSource; }
    public void setModuleSource(String moduleSource) { this.moduleSource = moduleSource; }

    public String getModuleDestination() { return moduleDestination; }
    public void setModuleDestination(String moduleDestination) { this.moduleDestination = moduleDestination; }

    public String getProperties() { return properties; }
    public void setProperties(String properties) { this.properties = properties; }

    public String getReqMessage() { return reqMessage; }
    public void setReqMessage(String reqMessage) { this.reqMessage = reqMessage; }

    public String getResMessage() { return resMessage; }
    public void setResMessage(String resMessage) { this.resMessage = resMessage; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getErrorDetails() { return errorDetails; }
    public void setErrorDetails(String errorDetails) { this.errorDetails = errorDetails; }

    public LocalDateTime getInitialDate() { return initialDate; }
    public void setInitialDate(LocalDateTime initialDate) { this.initialDate = initialDate; }

    public LocalDateTime getCreateDate() { return createDate; }
    public void setCreateDate(LocalDateTime createDate) { this.createDate = createDate; }

    public String getLogType() { return logType; }
    public void setLogType(String logType) { this.logType = logType; }

    // --- equals/hashCode (no id here, so use all fields) ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LogListRowDTO)) return false;
        LogListRowDTO that = (LogListRowDTO) o;
        return Objects.equals(id, that.id)
            && Objects.equals(msgType, that.msgType)
            && Objects.equals(correlationId, that.correlationId)
            && Objects.equals(referenceType, that.referenceType)
            && Objects.equals(reference, that.reference)
            && Objects.equals(moduleSource, that.moduleSource)
            && Objects.equals(moduleDestination, that.moduleDestination)
            && Objects.equals(properties, that.properties)
            && Objects.equals(reqMessage, that.reqMessage)
            && Objects.equals(resMessage, that.resMessage)
            && Objects.equals(error, that.error)
            && Objects.equals(errorDetails, that.errorDetails)
            && Objects.equals(initialDate, that.initialDate)
            && Objects.equals(createDate, that.createDate)
            && Objects.equals(logType, that.logType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id, msgType, correlationId, referenceType, reference,
            moduleSource, moduleDestination, properties, reqMessage, resMessage,
            error, errorDetails, initialDate, createDate, logType
        );
    }

    // prettier-ignore

    @Override
    public String toString() {
        return "LogListRowDTO{" +
            "id=" + id +
            ", msgType='" + msgType + '\'' +
            ", correlationId='" + correlationId + '\'' +
            ", referenceType='" + referenceType + '\'' +
            ", reference='" + reference + '\'' +
            ", moduleSource='" + moduleSource + '\'' +
            ", moduleDestination='" + moduleDestination + '\'' +
            ", properties='" + properties + '\'' +
            ", reqMessage='" + reqMessage + '\'' +
            ", resMessage='" + resMessage + '\'' +
            ", error='" + error + '\'' +
            ", errorDetails='" + errorDetails + '\'' +
            ", initialDate=" + initialDate +
            ", createDate=" + createDate +
            ", logType='" + logType + '\'' +
            '}';
    }
}
