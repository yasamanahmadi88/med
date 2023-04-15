package com.behsa.medportal.service.impl;

import com.behsa.medportal.domain.MedAuthorityEntity;
import com.behsa.medportal.repository.MedAuthorityRepository;
import com.behsa.medportal.service.MedAuthorityService;
import com.behsa.medportal.service.dto.MedAuthorityDTO;
import com.behsa.medportal.service.mapper.MedAuthorityMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link MedAuthorityEntity}.
 */
@Service
@Transactional
public class MedAuthorityServiceImpl implements MedAuthorityService {

    private final Logger log = LoggerFactory.getLogger(MedAuthorityServiceImpl.class);

    private final MedAuthorityRepository medAuthorityRepository;

    private final MedAuthorityMapper medAuthorityMapper;

    public MedAuthorityServiceImpl(MedAuthorityRepository medAuthorityRepository, MedAuthorityMapper medAuthorityMapper) {
        this.medAuthorityRepository = medAuthorityRepository;
        this.medAuthorityMapper = medAuthorityMapper;
    }

    @Override
    public MedAuthorityDTO save(MedAuthorityDTO medAuthorityDTO) {
        log.debug("Request to save MedAuthority : {}", medAuthorityDTO);
        MedAuthorityEntity medAuthorityEntity = medAuthorityMapper.toEntity(medAuthorityDTO);
        if (medAuthorityEntity.getParent().getId() == null)
            medAuthorityEntity.setParent(null);
        medAuthorityEntity = medAuthorityRepository.save(medAuthorityEntity);
        return medAuthorityMapper.toDto(medAuthorityEntity);
    }

    @Override
    public MedAuthorityDTO update(MedAuthorityDTO medAuthorityDTO) {
        log.debug("Request to update MedAuthority : {}", medAuthorityDTO);
        MedAuthorityEntity medAuthorityEntity = medAuthorityMapper.toEntity(medAuthorityDTO);
        if (medAuthorityEntity.getParent().getId() == null)
            medAuthorityEntity.setParent(null);
        medAuthorityEntity = medAuthorityRepository.save(medAuthorityEntity);
        return medAuthorityMapper.toDto(medAuthorityEntity);
    }

    @Override
    public Optional<MedAuthorityDTO> partialUpdate(MedAuthorityDTO medAuthorityDTO) {
        log.debug("Request to partially update MedAuthority : {}", medAuthorityDTO);

        return medAuthorityRepository
            .findById(medAuthorityDTO.getId())
            .map(existingMedAuthority -> {
                medAuthorityMapper.partialUpdate(existingMedAuthority, medAuthorityDTO);

                return existingMedAuthority;
            })
            .map(medAuthorityRepository::save)
            .map(medAuthorityMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MedAuthorityDTO> findAll(Pageable pageable) {
        log.debug("Request to get all MedAuthorities");
        return medAuthorityRepository.findAll(pageable).map(medAuthorityMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MedAuthorityDTO> findOne(Long id) {
        log.debug("Request to get MedAuthority : {}", id);
        return medAuthorityRepository.findById(id).map(medAuthorityMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete MedAuthority : {}", id);
        medAuthorityRepository.deleteById(id);
    }
}
