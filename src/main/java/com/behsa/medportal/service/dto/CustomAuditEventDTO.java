package com.behsa.medportal.service.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link com.behsa.medportal.domain.CustomAuditEventEntity} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CustomAuditEventDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 50)
    private String principal;

    private LocalDateTime eventDate;

    @Size(max = 255)
    private String eventType;

    private Map<String, String> data;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Map<String, String> getData() {
        return data;
    }

    public CustomAuditEventDTO setData(Map<String, String> data) {
        this.data = data;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CustomAuditEventDTO)) {
            return false;
        }

        CustomAuditEventDTO customAuditEventDTO = (CustomAuditEventDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, customAuditEventDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CustomAuditEventDTO{" +
            "id=" + getId() +
            ", principal='" + getPrincipal() + "'" +
            ", eventDate='" + getEventDate() + "'" +
            ", eventType='" + getEventType() + "'" +
            "}";
    }
}
