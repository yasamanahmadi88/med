package com.behsa.medportal.med.repository;

import com.behsa.medportal.med.domain.ProductEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ProductEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long>, JpaSpecificationExecutor<ProductEntity> {}
