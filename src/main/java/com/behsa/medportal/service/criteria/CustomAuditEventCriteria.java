package com.behsa.medportal.service.criteria;

import java.io.Serializable;
import java.util.Objects;

import com.behsa.medportal.filter.LocalDateTimeFilter;
import org.springdoc.api.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.behsa.medportal.domain.CustomAuditEventEntity} entity. This class is used
 * in {@link com.behsa.medportal.web.rest.CustomAuditEventResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /custom-audit-events?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CustomAuditEventCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter principal;

    private LocalDateTimeFilter eventDate;

    private StringFilter eventType;

    private Boolean distinct;

    public CustomAuditEventCriteria() {}

    public CustomAuditEventCriteria(CustomAuditEventCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.principal = other.principal == null ? null : other.principal.copy();
        this.eventDate = other.eventDate == null ? null : other.eventDate.copy();
        this.eventType = other.eventType == null ? null : other.eventType.copy();
        this.distinct = other.distinct;
    }

    @Override
    public CustomAuditEventCriteria copy() {
        return new CustomAuditEventCriteria(this);
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

    public StringFilter getPrincipal() {
        return principal;
    }

    public StringFilter principal() {
        if (principal == null) {
            principal = new StringFilter();
        }
        return principal;
    }

    public void setPrincipal(StringFilter principal) {
        this.principal = principal;
    }

    public LocalDateTimeFilter getEventDate() {
        return eventDate;
    }

    public LocalDateTimeFilter eventDate() {
        if (eventDate == null) {
            eventDate = new LocalDateTimeFilter();
        }
        return eventDate;
    }

    public void setEventDate(LocalDateTimeFilter eventDate) {
        this.eventDate = eventDate;
    }

    public StringFilter getEventType() {
        return eventType;
    }

    public StringFilter eventType() {
        if (eventType == null) {
            eventType = new StringFilter();
        }
        return eventType;
    }

    public void setEventType(StringFilter eventType) {
        this.eventType = eventType;
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
        final CustomAuditEventCriteria that = (CustomAuditEventCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(principal, that.principal) &&
            Objects.equals(eventDate, that.eventDate) &&
            Objects.equals(eventType, that.eventType) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, principal, eventDate, eventType, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CustomAuditEventCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (principal != null ? "principal=" + principal + ", " : "") +
            (eventDate != null ? "eventDate=" + eventDate + ", " : "") +
            (eventType != null ? "eventType=" + eventType + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
