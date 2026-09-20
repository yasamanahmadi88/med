package com.behsa.medportal.service.impl;

import com.behsa.medportal.domain.ResourceAuthorityEntity;
import com.behsa.medportal.repository.ResourceAuthorityRepository;
import com.behsa.medportal.service.ResourceAuthorityService;
import com.behsa.medportal.service.dto.ResourceAuthorityDTO;
import com.behsa.medportal.service.mapper.ResourceAuthorityMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link ResourceAuthorityEntity}.
 */
@Service
@Transactional
public class ResourceAuthorityServiceImpl implements ResourceAuthorityService {

    private final Logger log = LoggerFactory.getLogger(ResourceAuthorityServiceImpl.class);

    private final ResourceAuthorityRepository resourceAuthorityRepository;

    private final ResourceAuthorityMapper resourceAuthorityMapper;

    public ResourceAuthorityServiceImpl(
        ResourceAuthorityRepository resourceAuthorityRepository,
        ResourceAuthorityMapper resourceAuthorityMapper
    ) {
        this.resourceAuthorityRepository = resourceAuthorityRepository;
        this.resourceAuthorityMapper = resourceAuthorityMapper;
    }

    @Override
    public ResourceAuthorityDTO save(ResourceAuthorityDTO resourceAuthorityDTO) {
        log.debug("Request to save ResourceAuthority : {}", resourceAuthorityDTO);
        ResourceAuthorityEntity resourceAuthorityEntity = resourceAuthorityMapper.toEntity(resourceAuthorityDTO);
        resourceAuthorityEntity = resourceAuthorityRepository.save(resourceAuthorityEntity);
        return resourceAuthorityMapper.toDto(resourceAuthorityEntity);
    }

    @Override
    public ResourceAuthorityDTO update(ResourceAuthorityDTO resourceAuthorityDTO) {
        log.debug("Request to update ResourceAuthority : {}", resourceAuthorityDTO);
        ResourceAuthorityEntity resourceAuthorityEntity = resourceAuthorityMapper.toEntity(resourceAuthorityDTO);
        resourceAuthorityEntity = resourceAuthorityRepository.save(resourceAuthorityEntity);
        return resourceAuthorityMapper.toDto(resourceAuthorityEntity);
    }

    @Override
    public Optional<ResourceAuthorityDTO> partialUpdate(ResourceAuthorityDTO resourceAuthorityDTO) {
        log.debug("Request to partially update ResourceAuthority : {}", resourceAuthorityDTO);

        return resourceAuthorityRepository
            .findById(resourceAuthorityDTO.getId())
            .map(existingResourceAuthority -> {
                resourceAuthorityMapper.partialUpdate(existingResourceAuthority, resourceAuthorityDTO);

                return existingResourceAuthority;
            })
            .map(resourceAuthorityRepository::save)
            .map(resourceAuthorityMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ResourceAuthorityDTO> findAll(Pageable pageable) {
        log.debug("Request to get all ResourceAuthorities");
        return resourceAuthorityRepository.findAll(pageable).map(resourceAuthorityMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ResourceAuthorityDTO> findOne(Long id) {
        log.debug("Request to get ResourceAuthority : {}", id);
        return resourceAuthorityRepository.findById(id).map(resourceAuthorityMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete ResourceAuthority : {}", id);
        resourceAuthorityRepository.deleteById(id);
    }
}
