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

    public void log(final String eventName, Map<String, Object> data) {
        data.put("jwt", SecurityUtils.getCurrentUserJWT().get());
        AuditEvent event = new AuditEvent(SecurityUtils.getCurrentUser().get().getUsername(), eventName, data);
        add(event);
    }

    @Override
    public List<AuditEvent> find(String principal, Instant after, String type) {
        Iterable<CustomAuditEventEntity> persistentAuditEvents =
            customAuditEventRepository.findByPrincipalAndEventDateAfterAndEventType(principal, after, type);
        return auditEventConverter.convertToAuditEvent(persistentAuditEvents);
    }
    /**
     * Search
     */

    public Page<AuditEvent> findByDates(LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable) {
        return customAuditEventRepository.findAllByEventDateBetween(fromDate, toDate, pageable)
            .map(auditEventConverter::convertToAuditEvent);
    }
    @Transactional(readOnly = true)
    public Page<AuditEvent> searchByText(String text, Pageable page) {
        log.debug("find by text : {}, page: {}", text, (Object) page);
        return customAuditEventRepository.searchByText("%".concat(text).concat("%"),"%".concat(text).concat("%"),"%".concat(text).concat("%"),page)
         .map(auditEventConverter::convertToAuditEvent);
    }

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
     * Truncate event data that might exceed column length.
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
