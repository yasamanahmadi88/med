package com.behsa.medportal.repository;

import com.behsa.medportal.domain.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Spring Data JPA repository for the ProductEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long>, JpaSpecificationExecutor<ProductEntity> {

    Page<ProductEntity> findAllByProductDescContainingIgnoreCaseOrProductNameContainingIgnoreCase(@NotNull @Size(max = 300) String productDesc, @NotNull @Size(max = 3) String productName, Pageable pageable);
}
