package com.behsa.medportal.service.criteria;

import java.io.Serializable;
import java.util.Objects;

import com.behsa.medportal.domain.VersionEntity;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link VersionEntity} entity. This class is used
 * in {@link com.behsa.medportal.web.rest.VersionResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /versions?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VersionCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter tableName;

    private StringFilter moduleName;

    private LongFilter tableVersion;

    private Boolean distinct;

    public VersionCriteria() {}

    public VersionCriteria(VersionCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.tableName = other.tableName == null ? null : other.tableName.copy();
        this.moduleName = other.moduleName == null ? null : other.moduleName.copy();
        this.tableVersion = other.tableVersion == null ? null : other.tableVersion.copy();
        this.distinct = other.distinct;
    }

    @Override
    public VersionCriteria copy() {
        return new VersionCriteria(this);
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

    public StringFilter getTableName() {
        return tableName;
    }

    public StringFilter tableName() {
        if (tableName == null) {
            tableName = new StringFilter();
        }
        return tableName;
    }

    public void setTableName(StringFilter tableName) {
        this.tableName = tableName;
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

    public LongFilter getTableVersion() {
        return tableVersion;
    }

    public LongFilter tableVersion() {
        if (tableVersion == null) {
            tableVersion = new LongFilter();
        }
        return tableVersion;
    }

    public void setTableVersion(LongFilter tableVersion) {
        this.tableVersion = tableVersion;
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
        final VersionCriteria that = (VersionCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(tableName, that.tableName) &&
            Objects.equals(moduleName, that.moduleName) &&
            Objects.equals(tableVersion, that.tableVersion) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tableName, moduleName, tableVersion, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VersionCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (tableName != null ? "tableName=" + tableName + ", " : "") +
            (moduleName != null ? "moduleName=" + moduleName + ", " : "") +
            (tableVersion != null ? "tableVersion=" + tableVersion + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
