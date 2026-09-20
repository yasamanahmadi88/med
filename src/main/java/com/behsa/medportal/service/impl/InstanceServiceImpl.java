package com.behsa.medportal.service.impl;

import com.behsa.medportal.domain.InstanceEntity;
import com.behsa.medportal.repository.InstanceRepository;
import com.behsa.medportal.service.InstanceService;
import com.behsa.medportal.service.dto.InstanceDTO;
import com.behsa.medportal.service.mapper.InstanceMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link InstanceEntity}.
 */
@Service
@Transactional
public class InstanceServiceImpl implements InstanceService {

    private final Logger log = LoggerFactory.getLogger(InstanceServiceImpl.class);

    private final InstanceRepository instanceRepository;

    private final InstanceMapper instanceMapper;

    public InstanceServiceImpl(InstanceRepository instanceRepository, InstanceMapper instanceMapper) {
        this.instanceRepository = instanceRepository;
        this.instanceMapper = instanceMapper;
    }

    @Override
    public InstanceDTO save(InstanceDTO instanceDTO) {
        log.debug("Request to save Instance : {}", instanceDTO);
        InstanceEntity instanceEntity = instanceMapper.toEntity(instanceDTO);
        instanceEntity = instanceRepository.save(instanceEntity);
        return instanceMapper.toDto(instanceEntity);
    }

    @Override
    public InstanceDTO update(InstanceDTO instanceDTO) {
        log.debug("Request to update Instance : {}", instanceDTO);
        InstanceEntity instanceEntity = instanceMapper.toEntity(instanceDTO);
        instanceEntity = instanceRepository.save(instanceEntity);
        return instanceMapper.toDto(instanceEntity);
    }

    @Override
    public Optional<InstanceDTO> partialUpdate(InstanceDTO instanceDTO) {
        log.debug("Request to partially update Instance : {}", instanceDTO);

        return instanceRepository
            .findById(instanceDTO.getId())
            .map(existingInstance -> {
                instanceMapper.partialUpdate(existingInstance, instanceDTO);

                return existingInstance;
            })
            .map(instanceRepository::save)
            .map(instanceMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InstanceDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Instances");
        return instanceRepository.findAll(pageable).map(instanceMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InstanceDTO> findOne(Long id) {
        log.debug("Request to get Instance : {}", id);
        return instanceRepository.findById(id).map(instanceMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Instance : {}", id);
        instanceRepository.deleteById(id);
    }
}
