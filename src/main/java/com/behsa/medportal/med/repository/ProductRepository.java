package com.behsa.medportal.med.repository;

import com.behsa.medportal.med.domain.FlowEntity;
import com.behsa.medportal.med.domain.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

/**
 * Spring Data JPA repository for the ProductEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long>, JpaSpecificationExecutor<ProductEntity> {

    Page<ProductEntity> findAllByProductDescContainingIgnoreCaseOrProductNameContainingIgnoreCase(@NotNull @Size(max = 300) String productDesc, @NotNull @Size(max = 3) String productName, Pageable pageable);
}
