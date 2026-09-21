package com.behsa.medportal.repository;

import com.behsa.medportal.domain.BpmnElementGroupEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BpmnElementGroupRepository extends JpaRepository<BpmnElementGroupEntity, Long> {

    @Query(
        value = """
            SELECT DISTINCT g.*
              FROM TBL_BPMN_ELEMENT_GROUP g
              JOIN TBL_OWNER_BPMN_GROUP og
                ON og.group_key = g.group_key
             WHERE og.owner_key = :ownerId
               AND og.enabled = 1
               AND g.enabled = 1
             ORDER BY g.group_key
            """,
        nativeQuery = true
    )
    List<BpmnElementGroupEntity> findEnabledByOwnerId(@Param("ownerId") Long ownerId);
}