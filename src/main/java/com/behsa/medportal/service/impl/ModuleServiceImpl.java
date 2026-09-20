package com.behsa.medportal.service.impl;

import com.behsa.medportal.domain.ModuleEntity;
import com.behsa.medportal.repository.ModuleRepository;
import com.behsa.medportal.service.ModuleService;
import com.behsa.medportal.service.dto.ModuleDTO;
import com.behsa.medportal.service.mapper.ModuleMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link ModuleEntity}.
 */
@Service
@Transactional
public class ModuleServiceImpl implements ModuleService {

    private final Logger log = LoggerFactory.getLogger(ModuleServiceImpl.class);

    private final ModuleRepository moduleRepository;

    private final ModuleMapper moduleMapper;

    public ModuleServiceImpl(ModuleRepository moduleRepository, ModuleMapper moduleMapper) {
        this.moduleRepository = moduleRepository;
        this.moduleMapper = moduleMapper;
    }

    @Override
    public ModuleDTO save(ModuleDTO moduleDTO) {
        log.debug("Request to save Module : {}", moduleDTO);
        ModuleEntity moduleEntity = moduleMapper.toEntity(moduleDTO);
        moduleEntity = moduleRepository.save(moduleEntity);
        return moduleMapper.toDto(moduleEntity);
    }

    @Override
    public ModuleDTO update(ModuleDTO moduleDTO) {
        log.debug("Request to update Module : {}", moduleDTO);
        ModuleEntity moduleEntity = moduleMapper.toEntity(moduleDTO);
        moduleEntity = moduleRepository.save(moduleEntity);
        return moduleMapper.toDto(moduleEntity);
    }

    @Override
    public Optional<ModuleDTO> partialUpdate(ModuleDTO moduleDTO) {
        log.debug("Request to partially update Module : {}", moduleDTO);

        return moduleRepository
            .findById(moduleDTO.getId())
            .map(existingModule -> {
                moduleMapper.partialUpdate(existingModule, moduleDTO);

                return existingModule;
            })
            .map(moduleRepository::save)
            .map(moduleMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ModuleDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Modules");
        return moduleRepository.findAll(pageable).map(moduleMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ModuleDTO> findOne(Long id) {
        log.debug("Request to get Module : {}", id);
        return moduleRepository.findById(id).map(moduleMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Module : {}", id);
        moduleRepository.deleteById(id);
    }
}
