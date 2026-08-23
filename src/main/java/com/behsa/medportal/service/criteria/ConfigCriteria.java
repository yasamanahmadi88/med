package com.behsa.medportal.service.criteria;

import java.io.Serializable;
import java.util.Objects;

import com.behsa.medportal.domain.ConfigEntity;
import org.springdoc.api.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link ConfigEntity} entity. This class is used
 * in {@link com.behsa.medportal.web.rest.ConfigResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /configs?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ConfigCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter property;

    private StringFilter pValue;

    private StringFilter commentDesc;

    private LongFilter moduleId;

    private Boolean distinct;

    public ConfigCriteria() {}

    public ConfigCriteria(ConfigCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.property = other.property == null ? null : other.property.copy();
        this.pValue = other.pValue == null ? null : other.pValue.copy();
        this.commentDesc = other.commentDesc == null ? null : other.commentDesc.copy();
        this.moduleId = other.moduleId == null ? null : other.moduleId.copy();
        this.distinct = other.distinct;
    }

    @Override
    public ConfigCriteria copy() {
        return new ConfigCriteria(this);
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

    public StringFilter getProperty() {
        return property;
    }

    public StringFilter property() {
        if (property == null) {
            property = new StringFilter();
        }
        return property;
    }

    public void setProperty(StringFilter property) {
        this.property = property;
    }

    public StringFilter getpValue() {
        return pValue;
    }

    public StringFilter pValue() {
        if (pValue == null) {
            pValue = new StringFilter();
        }
        return pValue;
    }

    public void setpValue(StringFilter pValue) {
        this.pValue = pValue;
    }

    public StringFilter getCommentDesc() {
        return commentDesc;
    }

    public StringFilter commentDesc() {
        if (commentDesc == null) {
            commentDesc = new StringFilter();
        }
        return commentDesc;
    }

    public void setCommentDesc(StringFilter commentDesc) {
        this.commentDesc = commentDesc;
    }

    public LongFilter getModuleId() {
        return moduleId;
    }

    public LongFilter moduleId() {
        if (moduleId == null) {
            moduleId = new LongFilter();
        }
        return moduleId;
    }

    public void setModuleId(LongFilter moduleId) {
        this.moduleId = moduleId;
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
        final ConfigCriteria that = (ConfigCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(property, that.property) &&
            Objects.equals(pValue, that.pValue) &&
            Objects.equals(commentDesc, that.commentDesc) &&
            Objects.equals(moduleId, that.moduleId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, property, pValue, commentDesc, moduleId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ConfigCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (property != null ? "property=" + property + ", " : "") +
            (pValue != null ? "pValue=" + pValue + ", " : "") +
            (commentDesc != null ? "commentDesc=" + commentDesc + ", " : "") +
            (moduleId != null ? "moduleId=" + moduleId + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
