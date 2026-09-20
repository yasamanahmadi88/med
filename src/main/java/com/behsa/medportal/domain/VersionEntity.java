package com.behsa.medportal.domain;

import java.io.Serializable;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * A VersionEntity.
 */
@Entity
@Table(name = "TBL_VERSIONS")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VersionEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "version_key")
    private Long id;

    @NotNull
    @Size(max = 100)
    @Column(name = "table_name", length = 100, nullable = false)
    private String tableName;

    @NotNull
    @Size(max = 50)
    @Column(name = "module_name", length = 50, nullable = false)
    private String moduleName;

    @NotNull
    @Column(name = "table_version", nullable = false)
    private Long tableVersion;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public VersionEntity id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTableName() {
        return this.tableName;
    }

    public VersionEntity tableName(String tableName) {
        this.setTableName(tableName);
        return this;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getModuleName() {
        return this.moduleName;
    }

    public VersionEntity moduleName(String moduleName) {
        this.setModuleName(moduleName);
        return this;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public Long getTableVersion() {
        return this.tableVersion;
    }

    public VersionEntity tableVersion(Long tableVersion) {
        this.setTableVersion(tableVersion);
        return this;
    }

    public void setTableVersion(Long tableVersion) {
        this.tableVersion = tableVersion;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VersionEntity)) {
            return false;
        }
        return id != null && id.equals(((VersionEntity) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VersionEntity{" +
            "id=" + getId() +
            ", tableName='" + getTableName() + "'" +
            ", moduleName='" + getModuleName() + "'" +
            ", tableVersion=" + getTableVersion() +
            "}";
    }
}
