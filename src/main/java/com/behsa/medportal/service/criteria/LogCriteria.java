package com.behsa.medportal.service.criteria;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import com.behsa.medportal.filter.LocalDateTimeFilter;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class LogCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;
    private StringFilter logType;
    private StringFilter msgType;
    private StringFilter correlationId;
    private StringFilter referenceType;
    private StringFilter reference;
    private StringFilter moduleSource;
    private StringFilter moduleDestination;
    private StringFilter error;
    private StringFilter errorDetails;
    private LongFilter isValidate;
    private LongFilter checkFlag;
    private LongFilter retryCount;
    private StringFilter retryMode;
    private StringFilter retryDestinationModule;
    private LocalDateTimeFilter initialDate;
    private LocalDateTimeFilter createDate;
    private LocalDateTimeFilter expireDate;
    private LocalDateTimeFilter insertDate;

    private Boolean distinct;

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getLogType() {
        return logType;
    }

    public void setLogType(StringFilter logType) {
        this.logType = logType;
    }

    public StringFilter getMsgType() {
        return msgType;
    }

    public void setMsgType(StringFilter msgType) {
        this.msgType = msgType;
    }

    public StringFilter getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(StringFilter correlationId) {
        this.correlationId = correlationId;
    }

    public StringFilter getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(StringFilter referenceType) {
        this.referenceType = referenceType;
    }

    public StringFilter getReference() {
        return reference;
    }

    public void setReference(StringFilter reference) {
        this.reference = reference;
    }

    public StringFilter getModuleSource() {
        return moduleSource;
    }

    public void setModuleSource(StringFilter moduleSource) {
        this.moduleSource = moduleSource;
    }

    public StringFilter getModuleDestination() {
        return moduleDestination;
    }

    public void setModuleDestination(StringFilter moduleDestination) {
        this.moduleDestination = moduleDestination;
    }

    public StringFilter getError() {
        return error;
    }

    public void setError(StringFilter error) {
        this.error = error;
    }

    public StringFilter getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(StringFilter errorDetails) {
        this.errorDetails = errorDetails;
    }

    public LongFilter getIsValidate() {
        return isValidate;
    }

    public void setIsValidate(LongFilter isValidate) {
        this.isValidate = isValidate;
    }

    public LongFilter getCheckFlag() {
        return checkFlag;
    }

    public void setCheckFlag(LongFilter checkFlag) {
        this.checkFlag = checkFlag;
    }

    public LongFilter getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(LongFilter retryCount) {
        this.retryCount = retryCount;
    }

    public StringFilter getRetryMode() {
        return retryMode;
    }

    public void setRetryMode(StringFilter retryMode) {
        this.retryMode = retryMode;
    }

    public StringFilter getRetryDestinationModule() {
        return retryDestinationModule;
    }

    public void setRetryDestinationModule(StringFilter retryDestinationModule) {
        this.retryDestinationModule = retryDestinationModule;
    }

    public LocalDateTimeFilter getInitialDate() {
        return initialDate;
    }

    public void setInitialDate(LocalDateTimeFilter initialDate) {
        this.initialDate = initialDate;
    }

    public LocalDateTimeFilter getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTimeFilter createDate) {
        this.createDate = createDate;
    }

    public LocalDateTimeFilter getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(LocalDateTimeFilter expireDate) {
        this.expireDate = expireDate;
    }

    public LocalDateTimeFilter getInsertDate() {
        return insertDate;
    }

    public void setInsertDate(LocalDateTimeFilter insertDate) {
        this.insertDate = insertDate;
    }

    public LogCriteria() {}
    public LogCriteria(LogCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.logType = other.logType == null ? null : other.logType.copy();
        this.msgType = other.msgType == null ? null : other.msgType.copy();
        this.correlationId = other.correlationId == null ? null : other.correlationId.copy();
        this.referenceType = other.referenceType == null ? null : other.referenceType.copy();
        this.reference = other.reference == null ? null : other.reference.copy();
        this.moduleSource = other.moduleSource == null ? null : other.moduleSource.copy();
        this.moduleDestination = other.moduleDestination == null ? null : other.moduleDestination.copy();
        this.error = other.error == null ? null : other.error.copy();
        this.errorDetails = other.errorDetails == null ? null : other.errorDetails.copy();
        this.isValidate = other.isValidate == null ? null : other.isValidate.copy();
        this.checkFlag = other.checkFlag == null ? null : other.checkFlag.copy();
        this.retryCount = other.retryCount == null ? null : other.retryCount.copy();
        this.retryMode = other.retryMode == null ? null : other.retryMode.copy();
        this.retryDestinationModule = other.retryDestinationModule == null ? null : other.retryDestinationModule.copy();
        this.initialDate = other.initialDate == null ? null : other.initialDate.copy();
        this.createDate = other.createDate == null ? null : other.createDate.copy();
        this.expireDate = other.expireDate == null ? null : other.expireDate.copy();
        this.insertDate = other.insertDate == null ? null : other.insertDate.copy();
        this.distinct = other.distinct;
    }
    @Override public LogCriteria copy() { return new LogCriteria(this); }

    // getters + lazy-initializer helpers (id(), logType(), ...)
    // generate like VersionCriteria for all fields above

    public Boolean getDistinct() { return distinct; }
    public void setDistinct(Boolean distinct) { this.distinct = distinct; }

    @Override public boolean equals(Object o) { /* generate like VersionCriteria */ return super.equals(o); }
    @Override public int hashCode() { /* generate */ return Objects.hash(
        id, logType, msgType, correlationId, referenceType, reference, moduleSource, moduleDestination,
        error, errorDetails, isValidate, checkFlag, retryCount, retryMode, retryDestinationModule,
        initialDate, createDate, expireDate, insertDate, distinct
    ); }

    @Override public String toString() { /* generate pretty like VersionCriteria */ return "LogCriteria{...}"; }
}
