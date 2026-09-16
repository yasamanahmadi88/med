package com.behsa.medportal.repository;

import com.behsa.medportal.domain.BpmnElementEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BpmnElementRepository extends JpaRepository<BpmnElementEntity, Long> {

    @Query(
        value = """
            SELECT DISTINCT e.*
              FROM TBL_BPMN_ELEMENT e
              JOIN TBL_BPMN_GROUP_ELEMENT ge ON ge.element_key = e.element_key
              JOIN TBL_BPMN_ELEMENT_GROUP g ON g.group_key = ge.group_key
              JOIN TBL_PRODUCT_BPMN_GROUP pg ON pg.group_key = g.group_key
             WHERE pg.product_key = :productId
               AND e.enabled = 1
               AND g.enabled = 1
             ORDER BY e.sort_order, e.element_key
            """,
        nativeQuery = true
    )
    List<BpmnElementEntity> findEnabledByProductId(@Param("productId") Long productId);
}
