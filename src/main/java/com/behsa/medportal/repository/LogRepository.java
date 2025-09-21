package com.behsa.medportal.repository;

import com.behsa.medportal.domain.LogEntity;
import com.behsa.medportal.service.dto.LogListRowDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

/**
 * Spring Data JPA repository for the LogEntity entity.
 */
@Repository
public interface LogRepository extends JpaRepository<LogEntity, Long>, JpaSpecificationExecutor<LogEntity> {

    // Your requested query as a paged projection
    @Query(
        value =
            "select new com.behsa.medportal.service.dto.LogListRowDTO(" +
                "  l.id," +
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
    Page<LogListRowDTO> findSummary(Pageable pageable);
}
