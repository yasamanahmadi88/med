package com.behsa.medportal.repository;

import com.behsa.medportal.domain.ResourceAuthorityEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for the ResourceAuthorityEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ResourceAuthorityRepository
    extends JpaRepository<ResourceAuthorityEntity, Long>, JpaSpecificationExecutor<ResourceAuthorityEntity> {

    Page<ResourceAuthorityEntity> findByMedAuthority_IdIn(List<Long> ids, Pageable pageable);
    Page<ResourceAuthorityEntity> findByIdOrResource_DisplayNameContainingIgnoreCaseOrResource_NameContainingIgnoreCase(
        Long id,
        String p1,
        String p2,
        Pageable pageable
    );

}
