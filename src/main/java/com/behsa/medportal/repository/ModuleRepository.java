package com.behsa.medportal.repository;

import com.behsa.medportal.domain.ModuleEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ModuleEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ModuleRepository extends JpaRepository<ModuleEntity, Long>, JpaSpecificationExecutor<ModuleEntity> {}
