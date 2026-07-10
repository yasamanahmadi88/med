package com.behsa.medportal.service;

import com.behsa.medportal.domain.CustomAuditEventEntity;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

@Component
public class CustomAuditEventConverter {

    /**
     * Convert a list of {@link CustomAuditEventEntity}s to a list of {@link AuditEvent}s.
     *
     * @param persistentAuditEvents the list to convert.
     * @return the converted list.
     */
    public List<AuditEvent> convertToAuditEvent(Iterable<CustomAuditEventEntity> persistentAuditEvents) {
        if (persistentAuditEvents == null) {
            return Collections.emptyList();
        }
        List<AuditEvent> auditEvents = new ArrayList<>();
        for (CustomAuditEventEntity persistentAuditEvent : persistentAuditEvents) {
            auditEvents.add(convertToAuditEvent(persistentAuditEvent));
        }
        return auditEvents;
    }

    /**
     * Convert a {@link CustomAuditEventEntity} to an {@link AuditEvent}.
     *
     * @param persistentAuditEvent the event to convert.
     * @return the converted list.
     */
    public AuditEvent convertToAuditEvent(CustomAuditEventEntity persistentAuditEvent) {
        if (persistentAuditEvent == null) {
            return null;
        }
        Clock clock = Clock.fixed(Instant.EPOCH, ZoneId.of("Asia/Tehran"));
        return new AuditEvent(persistentAuditEvent.getEventDate().atZone(clock.getZone()).toInstant(), persistentAuditEvent.getPrincipal(),
            persistentAuditEvent.getEventType(), convertDataToObjects(persistentAuditEvent.getData()));
    }

    /**
     * Internal conversion. This is needed to support the current SpringBoot actuator {@code AuditEventRepository} interface.
     *
     * @param data the data to convert.
     * @return a map of {@link String}, {@link Object}.
     */
    public Map<String, Object> convertDataToObjects(Map<String, String> data) {
        Map<String, Object> results = new HashMap<>();

        if (data != null) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                results.put(entry.getKey(), entry.getValue());
            }
        }
        return results;
    }

    /**
     * Internal conversion. This method will allow to save additional data.
     * By default, it will save the object as string.
     *
     * @param data the data to convert.
     * @return a map of {@link String}, {@link String}.
     */
    public Map<String, String> convertDataToStrings(Map<String, Object> data) {
        Map<String, String> results = new HashMap<>();

        if (data != null) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                // Extract the data that will be saved.
                if (entry.getValue() instanceof WebAuthenticationDetails) {
                    WebAuthenticationDetails authenticationDetails = (WebAuthenticationDetails) entry.getValue();
                    results.put("remoteAddress", authenticationDetails.getRemoteAddress());
                    results.put("sessionId", authenticationDetails.getSessionId());
                } else {
                    results.put(entry.getKey(), Objects.toString(entry.getValue()));
                }
            }
        }
        return results;
    }
}
