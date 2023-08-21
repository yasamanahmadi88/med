package com.behsa.medportal.service.mapper;

import com.behsa.medportal.domain.ProductEntity;
import com.behsa.medportal.service.dto.ProductDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ProductEntity} and its DTO {@link ProductDTO}.
 */
@Mapper(componentModel = "spring")
public interface ProductMapper extends EntityMapper<ProductDTO, ProductEntity> {}
