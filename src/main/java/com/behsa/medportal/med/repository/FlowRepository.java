package com.behsa.medportal.med.repository;

import com.behsa.medportal.med.domain.FlowEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Spring Data JPA repository for the FlowEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface FlowRepository extends JpaRepository<FlowEntity, Long>, JpaSpecificationExecutor<FlowEntity> {

    List<FlowEntity> findAllByFlowNameStartsWith(String flowName);
}
