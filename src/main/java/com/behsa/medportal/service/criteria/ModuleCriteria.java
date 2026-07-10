package com.behsa.medportal.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.behsa.medportal.domain.ModuleEntity} entity. This class is used
 * in {@link com.behsa.medportal.web.rest.ModuleResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /modules?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ModuleCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter moduleName;

    private StringFilter defaultPort;

    private StringFilter redisKeyPrefix;

    private LongFilter status;

    private StringFilter loggingMode;

    private StringFilter loggingFilter;

    private StringFilter dnsName;

    private LongFilter configsId;

    private Boolean distinct;

    public ModuleCriteria() {}

    public ModuleCriteria(ModuleCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.moduleName = other.moduleName == null ? null : other.moduleName.copy();
        this.defaultPort = other.defaultPort == null ? null : other.defaultPort.copy();
        this.redisKeyPrefix = other.redisKeyPrefix == null ? null : other.redisKeyPrefix.copy();
        this.status = other.status == null ? null : other.status.copy();
        this.loggingMode = other.loggingMode == null ? null : other.loggingMode.copy();
        this.loggingFilter = other.loggingFilter == null ? null : other.loggingFilter.copy();
        this.dnsName = other.dnsName == null ? null : other.dnsName.copy();
        this.configsId = other.configsId == null ? null : other.configsId.copy();
        this.distinct = other.distinct;
    }

    @Override
    public ModuleCriteria copy() {
        return new ModuleCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public LongFilter id() {
        if (id == null) {
            id = new LongFilter();
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getModuleName() {
        return moduleName;
    }

    public StringFilter moduleName() {
        if (moduleName == null) {
            moduleName = new StringFilter();
        }
        return moduleName;
    }

    public void setModuleName(StringFilter moduleName) {
        this.moduleName = moduleName;
    }

    public StringFilter getDefaultPort() {
        return defaultPort;
    }

    public StringFilter defaultPort() {
        if (defaultPort == null) {
            defaultPort = new StringFilter();
        }
        return defaultPort;
    }

    public void setDefaultPort(StringFilter defaultPort) {
        this.defaultPort = defaultPort;
    }

    public StringFilter getRedisKeyPrefix() {
        return redisKeyPrefix;
    }

    public StringFilter redisKeyPrefix() {
        if (redisKeyPrefix == null) {
            redisKeyPrefix = new StringFilter();
        }
        return redisKeyPrefix;
    }

    public void setRedisKeyPrefix(StringFilter redisKeyPrefix) {
        this.redisKeyPrefix = redisKeyPrefix;
    }

    public LongFilter getStatus() {
        return status;
    }

    public LongFilter status() {
        if (status == null) {
            status = new LongFilter();
        }
        return status;
    }

    public void setStatus(LongFilter status) {
        this.status = status;
    }

    public StringFilter getLoggingMode() {
        return loggingMode;
    }

    public StringFilter loggingMode() {
        if (loggingMode == null) {
            loggingMode = new StringFilter();
        }
        return loggingMode;
    }

    public void setLoggingMode(StringFilter loggingMode) {
        this.loggingMode = loggingMode;
    }

    public StringFilter getLoggingFilter() {
        return loggingFilter;
    }

    public StringFilter loggingFilter() {
        if (loggingFilter == null) {
            loggingFilter = new StringFilter();
        }
        return loggingFilter;
    }

    public void setLoggingFilter(StringFilter loggingFilter) {
        this.loggingFilter = loggingFilter;
    }

    public StringFilter getDnsName() {
        return dnsName;
    }

    public StringFilter dnsName() {
        if (dnsName == null) {
            dnsName = new StringFilter();
        }
        return dnsName;
    }

    public void setDnsName(StringFilter dnsName) {
        this.dnsName = dnsName;
    }

    public LongFilter getConfigsId() {
        return configsId;
    }

    public LongFilter configsId() {
        if (configsId == null) {
            configsId = new LongFilter();
        }
        return configsId;
    }

    public void setConfigsId(LongFilter configsId) {
        this.configsId = configsId;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ModuleCriteria that = (ModuleCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(moduleName, that.moduleName) &&
            Objects.equals(defaultPort, that.defaultPort) &&
            Objects.equals(redisKeyPrefix, that.redisKeyPrefix) &&
            Objects.equals(status, that.status) &&
            Objects.equals(loggingMode, that.loggingMode) &&
            Objects.equals(loggingFilter, that.loggingFilter) &&
            Objects.equals(dnsName, that.dnsName) &&
            Objects.equals(configsId, that.configsId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, moduleName, defaultPort, redisKeyPrefix, status, loggingMode, loggingFilter, dnsName, configsId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ModuleCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (moduleName != null ? "moduleName=" + moduleName + ", " : "") +
            (defaultPort != null ? "defaultPort=" + defaultPort + ", " : "") +
            (redisKeyPrefix != null ? "redisKeyPrefix=" + redisKeyPrefix + ", " : "") +
            (status != null ? "status=" + status + ", " : "") +
            (loggingMode != null ? "loggingMode=" + loggingMode + ", " : "") +
            (loggingFilter != null ? "loggingFilter=" + loggingFilter + ", " : "") +
            (dnsName != null ? "dnsName=" + dnsName + ", " : "") +
            (configsId != null ? "configsId=" + configsId + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
