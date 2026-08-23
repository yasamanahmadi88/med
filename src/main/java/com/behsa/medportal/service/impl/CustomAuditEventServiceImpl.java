package com.behsa.medportal.service.impl;

import com.behsa.medportal.domain.CustomAuditEventEntity;
import com.behsa.medportal.repository.CustomAuditEventRepository;
import com.behsa.medportal.service.CustomAuditEventService;
import com.behsa.medportal.service.dto.CustomAuditEventDTO;
import com.behsa.medportal.service.mapper.CustomAuditEventMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link CustomAuditEventEntity}.
 */
@Service
@Transactional
public class CustomAuditEventServiceImpl implements CustomAuditEventService {

    private final Logger log = LoggerFactory.getLogger(CustomAuditEventServiceImpl.class);

    private final CustomAuditEventRepository customAuditEventRepository;

    private final CustomAuditEventMapper customAuditEventMapper;

    public CustomAuditEventServiceImpl(
        CustomAuditEventRepository customAuditEventRepository,
        CustomAuditEventMapper customAuditEventMapper
    ) {
        this.customAuditEventRepository = customAuditEventRepository;
        this.customAuditEventMapper = customAuditEventMapper;
    }

    @Override
    public CustomAuditEventDTO save(CustomAuditEventDTO customAuditEventDTO) {
        log.debug("Request to save CustomAuditEvent : {}", customAuditEventDTO);
        CustomAuditEventEntity customAuditEventEntity = customAuditEventMapper.toEntity(customAuditEventDTO);
        customAuditEventEntity = customAuditEventRepository.save(customAuditEventEntity);
        return customAuditEventMapper.toDto(customAuditEventEntity);
    }

    @Override
    public CustomAuditEventDTO update(CustomAuditEventDTO customAuditEventDTO) {
        log.debug("Request to update CustomAuditEvent : {}", customAuditEventDTO);
        CustomAuditEventEntity customAuditEventEntity = customAuditEventMapper.toEntity(customAuditEventDTO);
        customAuditEventEntity = customAuditEventRepository.save(customAuditEventEntity);
        return customAuditEventMapper.toDto(customAuditEventEntity);
    }

    @Override
    public Optional<CustomAuditEventDTO> partialUpdate(CustomAuditEventDTO customAuditEventDTO) {
        log.debug("Request to partially update CustomAuditEvent : {}", customAuditEventDTO);

        return customAuditEventRepository
            .findById(customAuditEventDTO.getId())
            .map(existingCustomAuditEvent -> {
                customAuditEventMapper.partialUpdate(existingCustomAuditEvent, customAuditEventDTO);

                return existingCustomAuditEvent;
            })
            .map(customAuditEventRepository::save)
            .map(customAuditEventMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomAuditEventDTO> findAll(Pageable pageable) {
        log.debug("Request to get all CustomAuditEvents");
        return customAuditEventRepository.findAll(pageable).map(customAuditEventMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CustomAuditEventDTO> findOne(Long id) {
        log.debug("Request to get CustomAuditEvent : {}", id);
        return customAuditEventRepository.findById(id).map(customAuditEventMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete CustomAuditEvent : {}", id);
        customAuditEventRepository.deleteById(id);
    }
}
