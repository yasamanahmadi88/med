package com.behsa.medportal.med.repository;

import com.behsa.medportal.med.domain.ConfigEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ConfigEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ConfigRepository extends JpaRepository<ConfigEntity, Long>, JpaSpecificationExecutor<ConfigEntity> {}
