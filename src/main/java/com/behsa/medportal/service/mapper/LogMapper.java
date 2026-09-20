package com.behsa.medportal.service.mapper;

import com.behsa.medportal.domain.LogEntity;
import com.behsa.medportal.service.dto.LogDTO;
import org.mapstruct.*;

/**
 * Mapper for {@link LogEntity} <-> {@link LogDTO}.
 */
@Mapper(componentModel = "spring")
public interface LogMapper extends EntityMapper<LogDTO, LogEntity> {}
