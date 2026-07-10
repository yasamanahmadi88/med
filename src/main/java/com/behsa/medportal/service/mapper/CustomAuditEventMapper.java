package com.behsa.medportal.service.mapper;

import com.behsa.medportal.domain.CustomAuditEventEntity;
import com.behsa.medportal.service.dto.CustomAuditEventDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link CustomAuditEventEntity} and its DTO {@link CustomAuditEventDTO}.
 */
@Mapper(componentModel = "spring")
public interface CustomAuditEventMapper extends EntityMapper<CustomAuditEventDTO, CustomAuditEventEntity> {}
