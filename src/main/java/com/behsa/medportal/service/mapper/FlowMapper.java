package com.behsa.medportal.service.mapper;

import com.behsa.medportal.domain.FlowEntity;
import com.behsa.medportal.domain.ProductEntity;
import com.behsa.medportal.service.dto.FlowDTO;
import com.behsa.medportal.service.dto.ProductDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link FlowEntity} and its DTO {@link FlowDTO}.
 */
@Mapper(componentModel = "spring")
public interface FlowMapper extends EntityMapper<FlowDTO, FlowEntity> {

    FlowDTO toDto(FlowEntity s);

    @Named("productId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ProductDTO toDtoProductId(ProductEntity productEntity);
}
