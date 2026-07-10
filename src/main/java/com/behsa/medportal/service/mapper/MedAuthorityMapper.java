package com.behsa.medportal.service.mapper;

import com.behsa.medportal.domain.MedAuthorityEntity;
import com.behsa.medportal.service.dto.MedAuthorityDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link MedAuthorityEntity} and its DTO {@link MedAuthorityDTO}.
 */
@Mapper(componentModel = "spring")
public interface MedAuthorityMapper extends EntityMapper<MedAuthorityDTO, MedAuthorityEntity> {

    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(source = "parent.displayName", target = "parentDisplayName")
    MedAuthorityDTO toDto(MedAuthorityEntity authorityEntity);

    @Mapping(source = "parentId", target = "parent.id")
    MedAuthorityEntity toEntity(MedAuthorityDTO authorityDTO);
}
