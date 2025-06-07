package com.behsa.medportal.service.criteria;

import java.io.Serializable;
import java.util.Objects;

import com.behsa.medportal.domain.enumeration.Verb;
import org.springdoc.api.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.behsa.medportal.domain.ResourceAuthorityEntity} entity. This class is used
 * in {@link com.behsa.medportal.web.rest.ResourceAuthorityResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /resource-authorities?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ResourceAuthorityCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    public static class VerbFilter extends Filter<Verb> {

        public VerbFilter() {}

        public VerbFilter(VerbFilter filter) {
            super(filter);
        }

        @Override
        public VerbFilter copy() {
            return new VerbFilter(this);
        }
    }

    private LongFilter id;

    private VerbFilter verb;

    private LongFilter medAuthorityId;

    private LongFilter resourceId;

    private Boolean distinct;

    public ResourceAuthorityCriteria() {}

    public ResourceAuthorityCriteria(ResourceAuthorityCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.verb = other.verb == null ? null : other.verb.copy();
        this.medAuthorityId = other.medAuthorityId == null ? null : other.medAuthorityId.copy();
        this.resourceId = other.resourceId == null ? null : other.resourceId.copy();
        this.distinct = other.distinct;
    }

    @Override
    public ResourceAuthorityCriteria copy() {
        return new ResourceAuthorityCriteria(this);
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

    public VerbFilter getVerb() {
        return verb;
    }

    public VerbFilter verb() {
        if (verb == null) {
            verb = new VerbFilter();
        }
        return verb;
    }

    public void setVerb(VerbFilter verb) {
        this.verb = verb;
    }

    public LongFilter getMedAuthorityId() {
        return medAuthorityId;
    }

    public LongFilter medAuthorityId() {
        if (medAuthorityId == null) {
            medAuthorityId = new LongFilter();
        }
        return medAuthorityId;
    }

    public void setMedAuthorityId(LongFilter medAuthorityId) {
        this.medAuthorityId = medAuthorityId;
    }

    public LongFilter getResourceId() {
        return resourceId;
    }

    public LongFilter resourceId() {
        if (resourceId == null) {
            resourceId = new LongFilter();
        }
        return resourceId;
    }

    public void setResourceId(LongFilter resourceId) {
        this.resourceId = resourceId;
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
        final ResourceAuthorityCriteria that = (ResourceAuthorityCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(verb, that.verb) &&
            Objects.equals(medAuthorityId, that.medAuthorityId) &&
            Objects.equals(resourceId, that.resourceId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, verb, medAuthorityId, resourceId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ResourceAuthorityCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (verb != null ? "verb=" + verb + ", " : "") +
            (medAuthorityId != null ? "medAuthorityId=" + medAuthorityId + ", " : "") +
            (resourceId != null ? "resourceId=" + resourceId + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
