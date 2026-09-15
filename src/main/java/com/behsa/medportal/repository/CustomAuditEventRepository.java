package com.behsa.medportal.repository;

import com.behsa.medportal.domain.CustomAuditEventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for the {@link com.behsa.medportal.domain.CustomAuditEventEntity} entity.
 */
public interface CustomAuditEventRepository extends JpaRepository<CustomAuditEventEntity, Long>, JpaSpecificationExecutor<CustomAuditEventEntity> {
    /**
     * Search audit events by text across principal, event type, and data fields.
     * Uses SQL ESCAPE clause with backslash to safely handle literal % and _ characters in search patterns.
     * Requires input parameters to have wildcards escaped by the calling service (LoggerService).
     * CWE-89: Improper Neutralization of Special Elements used in an SQL Command.
     *
     * @param principal principal name pattern (with escaped LIKE wildcards)
     * @param type event type pattern (with escaped LIKE wildcards)
     * @param dValue data value pattern (with escaped LIKE wildcards)
     * @param pageable pagination parameters
     * @return paginated list of matching audit event entities
     */
    @Query( nativeQuery=true,
        value="select E.* from JHI_PERSISTENT_AUDIT_EVENT E left outer join JHI_PERSISTENT_AUDIT_EVT_DATA D ON E.EVENT_ID = D.EVENT_ID WHERE E.principal LIKE :PrincParam ESCAPE '\\' OR E.EVENT_TYPE LIKE :TypeParam ESCAPE '\\' OR D.VALUE LIKE :DValueParam ESCAPE '\\'",
        countQuery= "select count(E.EVENT_ID) from JHI_PERSISTENT_AUDIT_EVENT E left outer join JHI_PERSISTENT_AUDIT_EVT_DATA D ON E.EVENT_ID = D.EVENT_ID WHERE E.principal LIKE :PrincParam ESCAPE '\\' OR E.EVENT_TYPE LIKE :TypeParam ESCAPE '\\' OR D.VALUE LIKE :DValueParam ESCAPE '\\'"
    )
    Page<CustomAuditEventEntity> searchByText(@Param("PrincParam") String principal, @Param("TypeParam")String type,@Param("DValueParam")String dValue, Pageable pageable);

    List<CustomAuditEventEntity> findByPrincipal(String principal);

    List<CustomAuditEventEntity> findByPrincipalAndEventDateAfterAndEventType(String principal, Instant after, String type);

    Page<CustomAuditEventEntity> findAllByEventDateBetween(LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    List<CustomAuditEventEntity> findByEventDateBefore(Instant before);
}
