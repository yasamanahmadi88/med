package com.behsa.medportal.service.impl;

import com.behsa.medportal.domain.FlowEntity;
import com.behsa.medportal.repository.FlowRepository;
import com.behsa.medportal.service.FlowService;
import com.behsa.medportal.service.dto.FlowDTO;
import com.behsa.medportal.service.mapper.FlowMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link FlowEntity}.
 */
@Service
@Transactional
public class FlowServiceImpl implements FlowService {

    private final Logger log = LoggerFactory.getLogger(FlowServiceImpl.class);

    private final FlowRepository flowRepository;

    private final FlowMapper flowMapper;

    public FlowServiceImpl(FlowRepository flowRepository, FlowMapper flowMapper) {
        this.flowRepository = flowRepository;
        this.flowMapper = flowMapper;
    }

    @Override
    public FlowDTO save(FlowDTO flowDTO) {
        log.debug("Request to save Flow : {}", flowDTO);
        FlowEntity flowEntity = flowMapper.toEntity(flowDTO);
        flowEntity = flowRepository.save(flowEntity);
        return flowMapper.toDto(flowEntity);
    }

    @Override
    public FlowDTO update(FlowDTO flowDTO) {
        log.debug("Request to update Flow : {}", flowDTO);
        FlowEntity flowEntity = flowMapper.toEntity(flowDTO);
        flowEntity = flowRepository.save(flowEntity);
        return flowMapper.toDto(flowEntity);
    }

    @Override
    public Optional<FlowDTO> partialUpdate(FlowDTO flowDTO) {
        log.debug("Request to partially update Flow : {}", flowDTO);

        return flowRepository
            .findById(flowDTO.getId())
            .map(existingFlow -> {
                flowMapper.partialUpdate(existingFlow, flowDTO);

                return existingFlow;
            })
            .map(flowRepository::save)
            .map(flowMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FlowDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Flows");
        return flowRepository.findAll(pageable).map(flowMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FlowDTO> findOne(Long id) {
        log.debug("Request to get Flow : {}", id);
        return flowRepository.findById(id).map(flowMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Flow : {}", id);
        flowRepository.deleteById(id);
    }
}
