package com.behsa.medportal.repository;

import com.behsa.medportal.domain.FlowEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for the FlowEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface FlowRepository extends JpaRepository<FlowEntity, Long>, JpaSpecificationExecutor<FlowEntity> {

    List<FlowEntity> findAllByFlowNameStartsWith(String flowName);
}
