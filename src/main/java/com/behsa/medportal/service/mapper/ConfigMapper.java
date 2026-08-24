package com.behsa.medportal.service.mapper;

import com.behsa.medportal.domain.ConfigEntity;
import com.behsa.medportal.domain.ModuleEntity;
import com.behsa.medportal.service.dto.ConfigDTO;
import com.behsa.medportal.service.dto.ModuleDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ConfigEntity} and its DTO {@link ConfigDTO}.
 */
@Mapper(componentModel = "spring")
public interface ConfigMapper extends EntityMapper<ConfigDTO, ConfigEntity> {

    ConfigDTO toDto(ConfigEntity s);

    @Named("moduleId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ModuleDTO toDtoModuleId(ModuleEntity moduleEntity);
}
