package com.behsa.medportal.service.impl;

import com.behsa.medportal.domain.VersionEntity;
import com.behsa.medportal.repository.VersionRepository;
import com.behsa.medportal.service.VersionService;
import com.behsa.medportal.service.dto.VersionDTO;
import com.behsa.medportal.service.mapper.VersionMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link VersionEntity}.
 */
@Service
@Transactional
public class VersionServiceImpl implements VersionService {

    private final Logger log = LoggerFactory.getLogger(VersionServiceImpl.class);

    private final VersionRepository versionRepository;

    private final VersionMapper versionMapper;

    public VersionServiceImpl(VersionRepository versionRepository, VersionMapper versionMapper) {
        this.versionRepository = versionRepository;
        this.versionMapper = versionMapper;
    }

    @Override
    public VersionDTO save(VersionDTO versionDTO) {
        log.debug("Request to save Version : {}", versionDTO);
        VersionEntity versionEntity = versionMapper.toEntity(versionDTO);
        versionEntity = versionRepository.save(versionEntity);
        return versionMapper.toDto(versionEntity);
    }

    @Override
    public VersionDTO update(VersionDTO versionDTO) {
        log.debug("Request to update Version : {}", versionDTO);
        VersionEntity versionEntity = versionMapper.toEntity(versionDTO);
        versionEntity = versionRepository.save(versionEntity);
        return versionMapper.toDto(versionEntity);
    }

    @Override
    public Optional<VersionDTO> partialUpdate(VersionDTO versionDTO) {
        log.debug("Request to partially update Version : {}", versionDTO);

        return versionRepository
            .findById(versionDTO.getId())
            .map(existingVersion -> {
                versionMapper.partialUpdate(existingVersion, versionDTO);

                return existingVersion;
            })
            .map(versionRepository::save)
            .map(versionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VersionDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Versions");
        return versionRepository.findAll(pageable).map(versionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VersionDTO> findOne(Long id) {
        log.debug("Request to get Version : {}", id);
        return versionRepository.findById(id).map(versionMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Version : {}", id);
        versionRepository.deleteById(id);
    }
}
