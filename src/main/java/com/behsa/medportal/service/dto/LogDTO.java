package com.behsa.medportal.service.dto;

import com.behsa.medportal.domain.LogEntity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import jakarta.validation.constraints.*;

/**
 * A DTO for the {@link LogEntity} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class LogDTO implements Serializable {

    private Long id;

    @Size(max = 100)
    private String logType;

    @Size(max = 100)
    private String msgType;

    @Size(max = 100)
    private String correlationId;

    @Size(max = 50)
    private String sendId;

    @Size(max = 100)
    private String referenceType;

    @Size(max = 100)
    private String reference;

    @Min(0)
    @Max(1)
    private Long isValidate;

    private LocalDateTime initialDate;

    private LocalDateTime createDate;

    private LocalDateTime expireDate;

    @Size(max = 50)
    private String moduleSource;

    @Size(max = 50)
    private String moduleDestination;

    @Size(max = 300)
    private String sourceInstance;

    @Size(max = 100)
    private String destinationInstance;

    @Size(max = 200)
    private String error;

    @Size(max = 300)
    private String businessErrorDesc;

    @Size(max = 300)
    private String backendErrorDesc;

    @Size(max = 4000)
    private String errorDetails;

    private Long checkFlag;

    private LocalDateTime insertDate;

    // CLOBs (kept as String in DTO)
    private String properties;

    private String reqMessage;

    private String resMessage;

    @Size(max = 100)
    private String retryMode;

    private Long retryCount;

    @Size(max = 50)
    private String retryDestinationModule;

    @Size(max = 200)
    private String commentDesc;

    // --- getters / setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLogType() { return logType; }
    public void setLogType(String logType) { this.logType = logType; }

    public String getMsgType() { return msgType; }
    public void setMsgType(String msgType) { this.msgType = msgType; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public String getSendId() { return sendId; }
    public void setSendId(String sendId) { this.sendId = sendId; }

    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public Long getIsValidate() { return isValidate; }
    public void setIsValidate(Long isValidate) { this.isValidate = isValidate; }

    public LocalDateTime getInitialDate() { return initialDate; }
    public void setInitialDate(LocalDateTime initialDate) { this.initialDate = initialDate; }

    public LocalDateTime getCreateDate() { return createDate; }
    public void setCreateDate(LocalDateTime createDate) { this.createDate = createDate; }

    public LocalDateTime getExpireDate() { return expireDate; }
    public void setExpireDate(LocalDateTime expireDate) { this.expireDate = expireDate; }

    public String getModuleSource() { return moduleSource; }
    public void setModuleSource(String moduleSource) { this.moduleSource = moduleSource; }

    public String getModuleDestination() { return moduleDestination; }
    public void setModuleDestination(String moduleDestination) { this.moduleDestination = moduleDestination; }

    public String getSourceInstance() { return sourceInstance; }
    public void setSourceInstance(String sourceInstance) { this.sourceInstance = sourceInstance; }

    public String getDestinationInstance() { return destinationInstance; }
    public void setDestinationInstance(String destinationInstance) { this.destinationInstance = destinationInstance; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getBusinessErrorDesc() { return businessErrorDesc; }
    public void setBusinessErrorDesc(String businessErrorDesc) { this.businessErrorDesc = businessErrorDesc; }

    public String getBackendErrorDesc() { return backendErrorDesc; }
    public void setBackendErrorDesc(String backendErrorDesc) { this.backendErrorDesc = backendErrorDesc; }

    public String getErrorDetails() { return errorDetails; }
    public void setErrorDetails(String errorDetails) { this.errorDetails = errorDetails; }

    public Long getCheckFlag() { return checkFlag; }
    public void setCheckFlag(Long checkFlag) { this.checkFlag = checkFlag; }

    public LocalDateTime getInsertDate() { return insertDate; }
    public void setInsertDate(LocalDateTime insertDate) { this.insertDate = insertDate; }

    public String getProperties() { return properties; }
    public void setProperties(String properties) { this.properties = properties; }

    public String getReqMessage() { return reqMessage; }
    public void setReqMessage(String reqMessage) { this.reqMessage = reqMessage; }

    public String getResMessage() { return resMessage; }
    public void setResMessage(String resMessage) { this.resMessage = resMessage; }

    public String getRetryMode() { return retryMode; }
    public void setRetryMode(String retryMode) { this.retryMode = retryMode; }

    public Long getRetryCount() { return retryCount; }
    public void setRetryCount(Long retryCount) { this.retryCount = retryCount; }

    public String getRetryDestinationModule() { return retryDestinationModule; }
    public void setRetryDestinationModule(String retryDestinationModule) { this.retryDestinationModule = retryDestinationModule; }

    public String getCommentDesc() { return commentDesc; }
    public void setCommentDesc(String commentDesc) { this.commentDesc = commentDesc; }

    // --- equals / hashCode like VersionDTO (id-based) ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LogDTO)) return false;
        LogDTO that = (LogDTO) o;
        if (this.id == null) return false;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "LogDTO{" +
            "id=" + getId() +
            ", logType='" + getLogType() + "'" +
            ", msgType='" + getMsgType() + "'" +
            ", correlationId='" + getCorrelationId() + "'" +
            ", sendId='" + getSendId() + "'" +
            ", referenceType='" + getReferenceType() + "'" +
            ", reference='" + getReference() + "'" +
            ", isValidate=" + getIsValidate() +
            ", initialDate=" + getInitialDate() +
            ", createDate=" + getCreateDate() +
            ", expireDate=" + getExpireDate() +
            ", moduleSource='" + getModuleSource() + "'" +
            ", moduleDestination='" + getModuleDestination() + "'" +
            ", sourceInstance='" + getSourceInstance() + "'" +
            ", destinationInstance='" + getDestinationInstance() + "'" +
            ", error='" + getError() + "'" +
            ", businessErrorDesc='" + getBusinessErrorDesc() + "'" +
            ", backendErrorDesc='" + getBackendErrorDesc() + "'" +
            ", errorDetails='" + getErrorDetails() + "'" +
            ", checkFlag=" + getCheckFlag() +
            ", insertDate=" + getInsertDate() +
            ", properties='" + getProperties() + "'" +
            ", reqMessage='" + getReqMessage() + "'" +
            ", resMessage='" + getResMessage() + "'" +
            ", retryMode='" + getRetryMode() + "'" +
            ", retryCount=" + getRetryCount() +
            ", retryDestinationModule='" + getRetryDestinationModule() + "'" +
            ", commentDesc='" + getCommentDesc() + "'" +
            "}";
    }
}
