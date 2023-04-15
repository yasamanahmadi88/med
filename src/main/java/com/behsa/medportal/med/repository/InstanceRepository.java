package com.behsa.medportal.med.repository;

import com.behsa.medportal.med.domain.InstanceEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the InstanceEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface InstanceRepository extends JpaRepository<InstanceEntity, Long>, JpaSpecificationExecutor<InstanceEntity> {}
