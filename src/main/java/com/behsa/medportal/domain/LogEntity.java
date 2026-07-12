package com.behsa.medportal.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * A LogEntity.
 */
@Entity
@Table(name = "TBL_LOGS")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class LogEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "LOG_SEQ_GENERATOR")
    @SequenceGenerator(name = "LOG_SEQ_GENERATOR", sequenceName = "LOG_SEQ", allocationSize = 1)
    @Column(name = "log_key")
    private Long id;

    @Size(max = 100) @Column(name = "log_type", length = 100)
    private String logType;

    @Size(max = 100) @Column(name = "msg_type", length = 100)
    private String msgType;

    @Size(max = 100) @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Size(max = 50) @Column(name = "send_id", length = 50)
    private String sendId;

    @Size(max = 100) @Column(name = "reference_type", length = 100)
    private String referenceType;

    @Size(max = 100) @Column(name = "reference", length = 100)
    private String reference;

    @Min(0) @Max(1) @Column(name = "is_validate")
    private Long isValidate;

    @Column(name = "initial_date")
    private LocalDateTime initialDate;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "expire_date")
    private LocalDateTime expireDate;

    @Size(max = 50) @Column(name = "module_source", length = 50)
    private String moduleSource;

    @Size(max = 50) @Column(name = "module_destination", length = 50)
    private String moduleDestination;

    @Size(max = 300) @Column(name = "source_instance", length = 300)
    private String sourceInstance;

    @Size(max = 100) @Column(name = "destination_instance", length = 100)
    private String destinationInstance;

    @Size(max = 200) @Column(name = "error", length = 200)
    private String error;

    @Size(max = 300) @Column(name = "business_error_desc", length = 300)
    private String businessErrorDesc;

    @Size(max = 300) @Column(name = "backend_error_desc", length = 300)
    private String backendErrorDesc;

    @Size(max = 4000) @Column(name = "error_details", length = 4000)
    private String errorDetails; // VARCHAR2(4000) -> not CLOB

    @Column(name = "check_flag")
    private Long checkFlag;

    @Column(name = "insert_date")
    private LocalDateTime insertDate;

    @Lob @Basic(fetch = FetchType.LAZY)
    @Column(name = "properties")
    private String properties; // CLOB

    @Lob @Basic(fetch = FetchType.LAZY)
    @Column(name = "req_message")
    private String reqMessage; // CLOB

    @Lob @Basic(fetch = FetchType.LAZY)
    @Column(name = "res_message")
    private String resMessage; // CLOB

    @Size(max = 100) @Column(name = "retry_mode", length = 100)
    private String retryMode;

    @Column(name = "retry_count")
    private Long retryCount;

    @Size(max = 50) @Column(name = "retry_destination_module", length = 50)
    private String retryDestinationModule;

    @Size(max = 200) @Column(name = "comment_desc", length = 200)
    private String commentDesc;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    // Getters + fluent setters (same style as VersionEntity)
    public Long getId() { return id; }
    public LogEntity id(Long id) { this.id = id; return this; }

    public String getLogType() { return logType; }
    public LogEntity logType(String v) { this.logType = v; return this; }

    public String getMsgType() { return msgType; }
    public LogEntity msgType(String v) { this.msgType = v; return this; }

    public String getCorrelationId() { return correlationId; }
    public LogEntity correlationId(String v) { this.correlationId = v; return this; }

    public String getSendId() { return sendId; }
    public LogEntity sendId(String v) { this.sendId = v; return this; }

    public String getReferenceType() { return referenceType; }
    public LogEntity referenceType(String v) { this.referenceType = v; return this; }

    public String getReference() { return reference; }
    public LogEntity reference(String v) { this.reference = v; return this; }

    public Long getIsValidate() { return isValidate; }
    public LogEntity isValidate(Long v) { this.isValidate = v; return this; }

    public LocalDateTime getInitialDate() { return initialDate; }
    public LogEntity initialDate(LocalDateTime v) { this.initialDate = v; return this; }

    public LocalDateTime getCreateDate() { return createDate; }
    public LogEntity createDate(LocalDateTime v) { this.createDate = v; return this; }

    public LocalDateTime getExpireDate() { return expireDate; }
    public LogEntity expireDate(LocalDateTime v) { this.expireDate = v; return this; }

    public String getModuleSource() { return moduleSource; }
    public LogEntity moduleSource(String v) { this.moduleSource = v; return this; }

    public String getModuleDestination() { return moduleDestination; }
    public LogEntity moduleDestination(String v) { this.moduleDestination = v; return this; }

    public String getSourceInstance() { return sourceInstance; }
    public LogEntity sourceInstance(String v) { this.sourceInstance = v; return this; }

    public String getDestinationInstance() { return destinationInstance; }
    public LogEntity destinationInstance(String v) { this.destinationInstance = v; return this; }

    public String getError() { return error; }
    public LogEntity error(String v) { this.error = v; return this; }

    public String getBusinessErrorDesc() { return businessErrorDesc; }
    public LogEntity businessErrorDesc(String v) { this.businessErrorDesc = v; return this; }

    public String getBackendErrorDesc() { return backendErrorDesc; }
    public LogEntity backendErrorDesc(String v) { this.backendErrorDesc = v; return this; }

    public String getErrorDetails() { return errorDetails; }
    public LogEntity errorDetails(String v) { this.errorDetails = v; return this; }

    public Long getCheckFlag() { return checkFlag; }
    public LogEntity checkFlag(Long v) { this.checkFlag = v; return this; }

    public LocalDateTime getInsertDate() { return insertDate; }
    public LogEntity insertDate(LocalDateTime v) { this.insertDate = v; return this; }

    public String getProperties() { return properties; }
    public LogEntity properties(String v) { this.properties = v; return this; }

    public String getReqMessage() { return reqMessage; }
    public LogEntity reqMessage(String v) { this.reqMessage = v; return this; }

    public String getResMessage() { return resMessage; }
    public LogEntity resMessage(String v) { this.resMessage = v; return this; }

    public String getRetryMode() { return retryMode; }
    public LogEntity retryMode(String v) { this.retryMode = v; return this; }

    public Long getRetryCount() { return retryCount; }
    public LogEntity retryCount(Long v) { this.retryCount = v; return this; }

    public String getRetryDestinationModule() { return retryDestinationModule; }
    public LogEntity retryDestinationModule(String v) { this.retryDestinationModule = v; return this; }

    public String getCommentDesc() { return commentDesc; }
    public LogEntity commentDesc(String v) { this.commentDesc = v; return this; }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LogEntity)) return false;
        return id != null && id.equals(((LogEntity) o).id);
    }
    @Override public int hashCode() { return getClass().hashCode(); }

    @Override public String toString() {
        return "LogEntity{" +
            "id=" + id +
            ", logType='" + logType + '\'' +
            ", msgType='" + msgType + '\'' +
            ", correlationId='" + correlationId + '\'' +
            ", referenceType='" + referenceType + '\'' +
            ", reference='" + reference + '\'' +
            ", moduleSource='" + moduleSource + '\'' +
            ", moduleDestination='" + moduleDestination + '\'' +
            ", createDate=" + createDate +
            "}";
    }
}
