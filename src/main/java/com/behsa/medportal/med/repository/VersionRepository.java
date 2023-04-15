package com.behsa.medportal.med.repository;

import com.behsa.medportal.med.domain.VersionEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the VersionEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface VersionRepository extends JpaRepository<VersionEntity, Long>, JpaSpecificationExecutor<VersionEntity> {}
