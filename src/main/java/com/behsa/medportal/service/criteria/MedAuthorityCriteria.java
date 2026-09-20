package com.behsa.medportal.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.behsa.medportal.domain.MedAuthorityEntity} entity. This class is used
 * in {@link com.behsa.medportal.web.rest.MedAuthorityResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /med-authorities?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MedAuthorityCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter name;

    private StringFilter displayName;

    private LongFilter parentId;

    private LongFilter resourceAuthoritiesId;

    private Boolean distinct;

    public MedAuthorityCriteria() {}

    public MedAuthorityCriteria(MedAuthorityCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.name = other.name == null ? null : other.name.copy();
        this.displayName = other.displayName == null ? null : other.displayName.copy();
        this.parentId = other.parentId == null ? null : other.parentId.copy();
        this.resourceAuthoritiesId = other.resourceAuthoritiesId == null ? null : other.resourceAuthoritiesId.copy();
        this.distinct = other.distinct;
    }

    @Override
    public MedAuthorityCriteria copy() {
        return new MedAuthorityCriteria(this);
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

    public StringFilter getName() {
        return name;
    }

    public StringFilter name() {
        if (name == null) {
            name = new StringFilter();
        }
        return name;
    }

    public void setName(StringFilter name) {
        this.name = name;
    }

    public StringFilter getDisplayName() {
        return displayName;
    }

    public StringFilter displayName() {
        if (displayName == null) {
            displayName = new StringFilter();
        }
        return displayName;
    }

    public void setDisplayName(StringFilter displayName) {
        this.displayName = displayName;
    }

    public LongFilter getParentId() {
        return parentId;
    }

    public LongFilter parentId() {
        if (parentId == null) {
            parentId = new LongFilter();
        }
        return parentId;
    }

    public void setParentId(LongFilter parentId) {
        this.parentId = parentId;
    }

    public LongFilter getResourceAuthoritiesId() {
        return resourceAuthoritiesId;
    }

    public LongFilter resourceAuthoritiesId() {
        if (resourceAuthoritiesId == null) {
            resourceAuthoritiesId = new LongFilter();
        }
        return resourceAuthoritiesId;
    }

    public void setResourceAuthoritiesId(LongFilter resourceAuthoritiesId) {
        this.resourceAuthoritiesId = resourceAuthoritiesId;
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
        final MedAuthorityCriteria that = (MedAuthorityCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(name, that.name) &&
            Objects.equals(displayName, that.displayName) &&
            Objects.equals(parentId, that.parentId) &&
            Objects.equals(resourceAuthoritiesId, that.resourceAuthoritiesId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, displayName, parentId, resourceAuthoritiesId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MedAuthorityCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (name != null ? "name=" + name + ", " : "") +
            (displayName != null ? "displayName=" + displayName + ", " : "") +
            (parentId != null ? "parentId=" + parentId + ", " : "") +
            (resourceAuthoritiesId != null ? "resourceAuthoritiesId=" + resourceAuthoritiesId + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
