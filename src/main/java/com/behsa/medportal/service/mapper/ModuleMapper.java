package com.behsa.medportal.service.mapper;

import com.behsa.medportal.domain.ModuleEntity;
import com.behsa.medportal.service.dto.ModuleDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ModuleEntity} and its DTO {@link ModuleDTO}.
 */
@Mapper(componentModel = "spring")
public interface ModuleMapper extends EntityMapper<ModuleDTO, ModuleEntity> {}
