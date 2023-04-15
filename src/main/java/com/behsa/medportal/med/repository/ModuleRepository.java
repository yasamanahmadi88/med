package com.behsa.medportal.med.repository;

import com.behsa.medportal.med.domain.ModuleEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ModuleEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ModuleRepository extends JpaRepository<ModuleEntity, Long>, JpaSpecificationExecutor<ModuleEntity> {}
