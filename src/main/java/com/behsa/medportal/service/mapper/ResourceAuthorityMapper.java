package com.behsa.medportal.service.mapper;

import com.behsa.medportal.domain.MedAuthorityEntity;
import com.behsa.medportal.domain.ResourceAuthorityEntity;
import com.behsa.medportal.domain.ResourceEntity;
import com.behsa.medportal.service.dto.MedAuthorityDTO;
import com.behsa.medportal.service.dto.ResourceAuthorityDTO;
import com.behsa.medportal.service.dto.ResourceDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ResourceAuthorityEntity} and its DTO {@link ResourceAuthorityDTO}.
 */
@Mapper(componentModel = "spring")
public interface ResourceAuthorityMapper extends EntityMapper<ResourceAuthorityDTO, ResourceAuthorityEntity> {

    ResourceAuthorityDTO toDto(ResourceAuthorityEntity s);

    @Named("medAuthorityId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    MedAuthorityDTO toDtoMedAuthorityId(MedAuthorityEntity medAuthorityEntity);

    @Named("resourceId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ResourceDTO toDtoResourceId(ResourceEntity resourceEntity);
}
