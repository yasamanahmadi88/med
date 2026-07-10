package com.behsa.medportal.service.mapper;

import com.behsa.medportal.domain.InstanceEntity;
import com.behsa.medportal.service.dto.InstanceDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link InstanceEntity} and its DTO {@link InstanceDTO}.
 */
@Mapper(componentModel = "spring")
public interface InstanceMapper extends EntityMapper<InstanceDTO, InstanceEntity> {}
