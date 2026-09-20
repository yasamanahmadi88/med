package com.behsa.medportal.repository;

import com.behsa.medportal.domain.MedAuthorityEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for the MedAuthorityEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MedAuthorityRepository extends JpaRepository<MedAuthorityEntity, Long>, JpaSpecificationExecutor<MedAuthorityEntity> {
    List<MedAuthorityEntity> findByNameIn(List<String> string);
}
