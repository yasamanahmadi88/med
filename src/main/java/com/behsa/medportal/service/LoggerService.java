package com.behsa.medportal.service;

import com.behsa.medportal.domain.CustomAuditEventEntity;
import com.behsa.medportal.repository.CustomAuditEventRepository;
import com.behsa.medportal.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An implementation of Spring Boot's {@link AuditEventRepository}.
 */
@Repository
public class LoggerService implements AuditEventRepository {

    private static final String AUTHORIZATION_FAILURE = "AUTHORIZATION_FAILURE";

    /**
     * Should be the same as in Liquibase migration.
     */
    protected static final int EVENT_DATA_COLUMN_MAX_LENGTH = 255;

    private final CustomAuditEventRepository customAuditEventRepository;

    private final CustomAuditEventConverter auditEventConverter;

    private final Logger log = LoggerFactory.getLogger(getClass());

    public LoggerService(CustomAuditEventRepository customAuditEventRepository,
                         CustomAuditEventConverter auditEventConverter) {

        this.customAuditEventRepository = customAuditEventRepository;
        this.auditEventConverter = auditEventConverter;
    }

    /**
     * Create and persist an audit event for the current user with the given event name and data.
     * Automatically captures current user principal and JWT token (if available).
     *
     * @param eventName the type of event being logged
     * @param data event metadata and context information
     */
    public void log(final String eventName, Map<String, Object> data) {
        // JWT may be absent under @WithMockUser / non-JWT auth; audit must not fail the business call.
        data.put("jwt", SecurityUtils.getCurrentUserJWT().orElse(""));
        String principal = SecurityUtils
            .getCurrentUser()
            .map(user -> user.getUsername())
            .or(() -> SecurityUtils.getCurrentUserLogin())
            .orElse("anonymoususer");
        AuditEvent event = new AuditEvent(principal, eventName, data);
        add(event);
    }

    /**
     * Find audit events for a specific principal after a given timestamp and of a specific type.
     *
     * @param principal the user or system principal name
     * @param after the minimum event timestamp (inclusive)
     * @param type the event type to filter by
     * @return list of audit events matching the criteria
     */
    @Override
    public List<AuditEvent> find(String principal, Instant after, String type) {
        Iterable<CustomAuditEventEntity> persistentAuditEvents =
            customAuditEventRepository.findByPrincipalAndEventDateAfterAndEventType(principal, after, type);
        return auditEventConverter.convertToAuditEvent(persistentAuditEvents);
    }

    /**
     * Find audit events occurring between two date ranges.
     *
     * @param fromDate the start of the date range (inclusive)
     * @param toDate the end of the date range (inclusive)
     * @param pageable pagination parameters
     * @return paginated list of audit events within the date range
     */
    public Page<AuditEvent> findByDates(LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable) {
        return customAuditEventRepository.findAllByEventDateBetween(fromDate, toDate, pageable)
            .map(auditEventConverter::convertToAuditEvent);
    }

    /**
     * Search audit events by text across principal, event type, and data fields.
     * Escapes LIKE wildcards (%, _) to prevent SQL injection via uncontrolled pattern matching.
     * CWE-89: Improper Neutralization of Special Elements used in an SQL Command.
     *
     * @param text search text to match (automatically escaped for LIKE wildcards)
     * @param page pagination parameters
     * @return paginated list of matching audit events
     */
    @Transactional(readOnly = true)
    public Page<AuditEvent> searchByText(String text, Pageable page) {
        log.debug("find by text : {}, page: {}", text, (Object) page);
        // Escape LIKE wildcards to prevent injection
        String escapedText = text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return customAuditEventRepository.searchByText("%".concat(escapedText).concat("%"),"%".concat(escapedText).concat("%"),"%".concat(escapedText).concat("%"),page)
         .map(auditEventConverter::convertToAuditEvent);
    }

    /**
     * Persist an audit event to the database.
     * Skips authorization failures and anonymous user events.
     * Runs in a separate transaction to ensure audit logging is not rolled back with business logic.
     *
     * @param event the audit event to persist
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void add(AuditEvent event) {
        if (!AUTHORIZATION_FAILURE.equals(event.getType()) &&
            !"anonymoususer".equals(event.getPrincipal())) {

            CustomAuditEventEntity persistentAuditEvent = new CustomAuditEventEntity();
            persistentAuditEvent.setPrincipal(event.getPrincipal());
            persistentAuditEvent.setEventType(event.getType());
            persistentAuditEvent.setEventDate(LocalDateTime.ofInstant(event.getTimestamp(), ZoneId.systemDefault()));
            Map<String, String> eventData = auditEventConverter.convertDataToStrings(event.getData());
            persistentAuditEvent.setData(truncate(eventData));
            customAuditEventRepository.save(persistentAuditEvent);
        }
    }

    /**
     * Truncate event data values that exceed the maximum column length.
     * Prevents database constraint violations while preserving event context.
     *
     * @param data event data map with string values
     * @return map with truncated string values
     */
    private Map<String, String> truncate(Map<String, String> data) {
        Map<String, String> results = new HashMap<>();

        if (data != null) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                String value = entry.getValue();
                if (value != null) {
                    int length = value.length();
                    if (length > EVENT_DATA_COLUMN_MAX_LENGTH) {
                        value = value.substring(0, EVENT_DATA_COLUMN_MAX_LENGTH);
                        log.warn("Event data for {} too long ({}) has been truncated to {}. Consider increasing column width.",
                                 entry.getKey(), length, EVENT_DATA_COLUMN_MAX_LENGTH);
                    }
                }
                results.put(entry.getKey(), value);
            }
        }
        return results;
    }
}
