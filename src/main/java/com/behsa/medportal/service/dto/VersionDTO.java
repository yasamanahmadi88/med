package com.behsa.medportal.service.dto;

import com.behsa.medportal.domain.VersionEntity;

import java.io.Serializable;
import java.util.Objects;
import jakarta.validation.constraints.*;

/**
 * A DTO for the {@link VersionEntity} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VersionDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 100)
    private String tableName;

    @NotNull
    @Size(max = 50)
    private String moduleName;

    @NotNull
    private Long tableVersion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public Long getTableVersion() {
        return tableVersion;
    }

    public void setTableVersion(Long tableVersion) {
        this.tableVersion = tableVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VersionDTO)) {
            return false;
        }

        VersionDTO versionDTO = (VersionDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, versionDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VersionDTO{" +
            "id=" + getId() +
            ", tableName='" + getTableName() + "'" +
            ", moduleName='" + getModuleName() + "'" +
            ", tableVersion=" + getTableVersion() +
            "}";
    }
}
