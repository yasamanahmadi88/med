package com.behsa.medportal.service.dto;

import java.io.Serializable;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link com.behsa.medportal.domain.ModuleEntity} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ModuleDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 50)
    private String moduleName;

    @NotNull
    @Size(min = 4, max = 4)
    private String defaultPort;

    @NotNull
    @Size(min = 5, max = 5)
    private String redisKeyPrefix;

    @NotNull
    @Min(value = 0L)
    @Max(value = 1L)
    private Long status;

    @NotNull
    private String loggingMode;

    @Size(max = 500)
    private String loggingFilter;

    @NotNull
    @Size(max = 100)
    private String dnsName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getDefaultPort() {
        return defaultPort;
    }

    public void setDefaultPort(String defaultPort) {
        this.defaultPort = defaultPort;
    }

    public String getRedisKeyPrefix() {
        return redisKeyPrefix;
    }

    public void setRedisKeyPrefix(String redisKeyPrefix) {
        this.redisKeyPrefix = redisKeyPrefix;
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public String getLoggingMode() {
        return loggingMode;
    }

    public void setLoggingMode(String loggingMode) {
        this.loggingMode = loggingMode;
    }

    public String getLoggingFilter() {
        return loggingFilter;
    }

    public void setLoggingFilter(String loggingFilter) {
        this.loggingFilter = loggingFilter;
    }

    public String getDnsName() {
        return dnsName;
    }

    public void setDnsName(String dnsName) {
        this.dnsName = dnsName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ModuleDTO)) {
            return false;
        }

        ModuleDTO moduleDTO = (ModuleDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, moduleDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ModuleDTO{" +
            "id=" + getId() +
            ", moduleName='" + getModuleName() + "'" +
            ", defaultPort='" + getDefaultPort() + "'" +
            ", redisKeyPrefix='" + getRedisKeyPrefix() + "'" +
            ", status=" + getStatus() +
            ", loggingMode='" + getLoggingMode() + "'" +
            ", loggingFilter='" + getLoggingFilter() + "'" +
            ", dnsName='" + getDnsName() + "'" +
            "}";
    }
}
