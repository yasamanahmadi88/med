package com.behsa.medportal.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A ModuleEntity.
 */
@Entity
@Table(name = "MEDIATION.tbl_modules")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ModuleEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "AUTR_SEQ_GENERATOR")
    @SequenceGenerator(name = "AUTR_SEQ_GENERATOR", sequenceName = "MODULES_SEQ", allocationSize = 0)
    @Column(name = "module_key")
    private Long id;

    @NotNull
    @Size(max = 50)
    @Column(name = "module_name", length = 50, nullable = false)
    private String moduleName;

    @NotNull
    @Size(min = 4, max = 4)
    @Column(name = "default_port", length = 4, nullable = false)
    private String defaultPort;

    @NotNull
    @Size(min = 5, max = 5)
    @Column(name = "redis_key_prefix", length = 5, nullable = false)
    private String redisKeyPrefix;

    @NotNull
    @Min(value = 0L)
    @Max(value = 1L)
    @Column(name = "status", nullable = false)
    private Long status;

    @NotNull
    @Column(name = "logging_mode", nullable = false)
    private String loggingMode;

    @Size(max = 500)
    @Column(name = "logging_filter", length = 500)
    private String loggingFilter;

    @NotNull
    @Size(max = 100)
    @Column(name = "dns_name", length = 100, nullable = false)
    private String dnsName;

    @OneToMany(mappedBy = "module")
    @JsonIgnoreProperties(value = { "module" }, allowSetters = true)
    private Set<ConfigEntity> configs = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public ModuleEntity id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModuleName() {
        return this.moduleName;
    }

    public ModuleEntity moduleName(String moduleName) {
        this.setModuleName(moduleName);
        return this;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getDefaultPort() {
        return this.defaultPort;
    }

    public ModuleEntity defaultPort(String defaultPort) {
        this.setDefaultPort(defaultPort);
        return this;
    }

    public void setDefaultPort(String defaultPort) {
        this.defaultPort = defaultPort;
    }

    public String getRedisKeyPrefix() {
        return this.redisKeyPrefix;
    }

    public ModuleEntity redisKeyPrefix(String redisKeyPrefix) {
        this.setRedisKeyPrefix(redisKeyPrefix);
        return this;
    }

    public void setRedisKeyPrefix(String redisKeyPrefix) {
        this.redisKeyPrefix = redisKeyPrefix;
    }

    public Long getStatus() {
        return this.status;
    }

    public ModuleEntity status(Long status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public String getLoggingMode() {
        return this.loggingMode;
    }

    public ModuleEntity loggingMode(String loggingMode) {
        this.setLoggingMode(loggingMode);
        return this;
    }

    public void setLoggingMode(String loggingMode) {
        this.loggingMode = loggingMode;
    }

    public String getLoggingFilter() {
        return this.loggingFilter;
    }

    public ModuleEntity loggingFilter(String loggingFilter) {
        this.setLoggingFilter(loggingFilter);
        return this;
    }

    public void setLoggingFilter(String loggingFilter) {
        this.loggingFilter = loggingFilter;
    }

    public String getDnsName() {
        return this.dnsName;
    }

    public ModuleEntity dnsName(String dnsName) {
        this.setDnsName(dnsName);
        return this;
    }

    public void setDnsName(String dnsName) {
        this.dnsName = dnsName;
    }

    public Set<ConfigEntity> getConfigs() {
        return this.configs;
    }

    public void setConfigs(Set<ConfigEntity> configs) {
        if (this.configs != null) {
            this.configs.forEach(i -> i.setModule(null));
        }
        if (configs != null) {
            configs.forEach(i -> i.setModule(this));
        }
        this.configs = configs;
    }

    public ModuleEntity configs(Set<ConfigEntity> configs) {
        this.setConfigs(configs);
        return this;
    }

    public ModuleEntity addConfigs(ConfigEntity config) {
        this.configs.add(config);
        config.setModule(this);
        return this;
    }

    public ModuleEntity removeConfigs(ConfigEntity config) {
        this.configs.remove(config);
        config.setModule(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ModuleEntity)) {
            return false;
        }
        return id != null && id.equals(((ModuleEntity) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ModuleEntity{" +
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
