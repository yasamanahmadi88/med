package com.behsa.medportal.repository;

import com.behsa.medportal.domain.ResourceEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ResourceEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ResourceRepository extends JpaRepository<ResourceEntity, Long>, JpaSpecificationExecutor<ResourceEntity> {}
