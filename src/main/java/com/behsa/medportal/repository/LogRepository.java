package com.behsa.medportal.repository;

import com.behsa.medportal.domain.LogEntity;
import com.behsa.medportal.repository.projection.LogListRowProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the LogEntity entity.
 */
@Repository
public interface LogRepository extends JpaRepository<LogEntity, Long>, JpaSpecificationExecutor<LogEntity> {

    @Query(
        value =
            "select new com.behsa.medportal.repository.projection.LogListRowProjection(" +
                "  l.id, " +
                "  l.msgType, " +
                "  l.correlationId, " +
                "  l.referenceType, " +
                "  l.reference, " +
                "  l.moduleSource, " +
                "  l.moduleDestination, " +
                "  l.properties, " +
                "  l.reqMessage, " +
                "  l.resMessage, " +
                "  l.error, " +
                "  l.errorDetails, " +
                "  l.initialDate, " +
                "  l.createDate, " +
                "  l.logType" +
                ") " +
                "from LogEntity l " +
                "order by l.createDate desc",
        countQuery = "select count(l) from LogEntity l"
    )
    Page<LogListRowProjection> findSummary(Pageable pageable);

    @Query("select l from LogEntity l where lower(l.correlationId) like lower(concat('%', :correlationId, '%'))")
    Page<LogEntity> findAllByCorrelationIdContainingIgnoreCase(
        @Param("correlationId") String correlationId,
        Pageable pageable
    );
}
