package com.behsa.medportal.repository;

import com.behsa.medportal.domain.PortalOwnerEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PortalOwnerRepository extends JpaRepository<PortalOwnerEntity, Long> {

    @Query(
        value = """
            SELECT o.*
              FROM TBL_PORTAL_OWNER o
              JOIN TBL_PORTAL_CONFIGURATION c
                ON c.active_owner_key = o.owner_key
             WHERE c.config_key = 1
               AND o.enabled = 1
            """,
        nativeQuery = true
    )
    Optional<PortalOwnerEntity> findActiveOwner();
}