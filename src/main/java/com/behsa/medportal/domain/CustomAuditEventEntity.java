package com.behsa.medportal.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Persist AuditEvent managed by the Spring Boot actuator.
 *
 * @see org.springframework.boot.actuate.audit.AuditEvent
 */
@Entity
@Table(name = "JHI_AUDIT_EVENT")
public class CustomAuditEventEntity implements Serializable {

    private static final long serialVersionUID = 1L;


    @Id
    @SequenceGenerator(name = "PRS_ADT_SEQ_GENERATOR", sequenceName = "ADT_EVT_SEQ", allocationSize = 0)
    @GeneratedValue(generator = "PRS_ADT_SEQ_GENERATOR")
    @Column(name = "event_id")
    private Long id;
    @NotNull
    @Column(nullable = false)
    private String principal;

    @Column(name = "event_date")
    private LocalDateTime eventDate;

    @Column(name = "event_type")
    private String eventType;

    @ElementCollection
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @CollectionTable(name = "JHI_AUDIT_EVENT_DATA", joinColumns=@JoinColumn(name="event_id"))
    private Map<String, String> data = new HashMap<>();

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

    public void setEventDate(LocalDateTime auditEventDate) {
        this.eventDate = auditEventDate;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String auditEventType) {
        this.eventType = auditEventType;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CustomAuditEventEntity)) {
            return false;
        }
        return id != null && id.equals(((CustomAuditEventEntity) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "PersistentAuditEvent{" +
            "principal='" + principal + '\'' +
            ", auditEventDate=" + eventDate +
            ", auditEventType='" + eventType + '\'' +
            '}';
    }
}
