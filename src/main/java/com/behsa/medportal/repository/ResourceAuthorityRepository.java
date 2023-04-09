package com.behsa.medportal.repository;

import com.behsa.medportal.domain.ResourceAuthorityEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ResourceAuthorityEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ResourceAuthorityRepository
    extends JpaRepository<ResourceAuthorityEntity, Long>, JpaSpecificationExecutor<ResourceAuthorityEntity> {}
