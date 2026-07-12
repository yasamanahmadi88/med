package com.behsa.medportal.service.mapper;

import com.behsa.medportal.domain.ResourceEntity;
import com.behsa.medportal.service.dto.ResourceDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ResourceEntity} and its DTO {@link ResourceDTO}.
 */
@Mapper(componentModel = "spring")
public interface ResourceMapper extends EntityMapper<ResourceDTO, ResourceEntity> {}
