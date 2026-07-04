package com.behsa.medportal.service.mapper;

import com.behsa.medportal.domain.VersionEntity;
import com.behsa.medportal.service.dto.VersionDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link VersionEntity} and its DTO {@link VersionDTO}.
 */
@Mapper(componentModel = "spring")
public interface VersionMapper extends EntityMapper<VersionDTO, VersionEntity> {}
