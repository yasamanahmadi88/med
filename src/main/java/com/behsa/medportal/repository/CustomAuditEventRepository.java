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
    @Query( nativeQuery=true,
        value="select E.* from JHI_PERSISTENT_AUDIT_EVENT E left outer join JHI_PERSISTENT_AUDIT_EVT_DATA D ON E.EVENT_ID = D.EVENT_ID WHERE E.principal LIKE :PrincParam OR E.EVENT_TYPE LIKE :TypeParam OR D.VALUE LIKE :DValueParam",
        countQuery= "select count(E.EVENT_ID) from JHI_PERSISTENT_AUDIT_EVENT E left outer join JHI_PERSISTENT_AUDIT_EVT_DATA D ON E.EVENT_ID = D.EVENT_ID WHERE E.principal LIKE :PrincParam OR E.EVENT_TYPE LIKE :TypeParam OR D.VALUE LIKE :DValueParam"
    )
    Page<CustomAuditEventEntity> searchByText(@Param("PrincParam") String principal, @Param("TypeParam")String type,@Param("DValueParam")String dValue, Pageable pageable);

    List<CustomAuditEventEntity> findByPrincipal(String principal);

    List<CustomAuditEventEntity> findByPrincipalAndEventDateAfterAndEventType(String principal, Instant after, String type);

    Page<CustomAuditEventEntity> findAllByEventDateBetween(LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    List<CustomAuditEventEntity> findByEventDateBefore(Instant before);
}
