package com.behsa.medportal.service.impl;

import com.behsa.medportal.domain.LogEntity;
import com.behsa.medportal.repository.LogRepository;
import com.behsa.medportal.service.LogService;
import com.behsa.medportal.service.dto.LogDTO;
import com.behsa.medportal.service.dto.LogListRowDTO;
import com.behsa.medportal.service.mapper.LogMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LogServiceImpl implements LogService {

    private final Logger log = LoggerFactory.getLogger(LogServiceImpl.class);
    private final LogRepository logRepository;
    private final LogMapper logMapper;

    public LogServiceImpl(LogRepository logRepository, LogMapper logMapper) {
        this.logRepository = logRepository;
        this.logMapper = logMapper;
    }

    @Override
    public LogDTO save(LogDTO dto) {
        log.debug("Request to save Log : {}", dto);
        LogEntity entity = logMapper.toEntity(dto);
        entity = logRepository.save(entity);
        return logMapper.toDto(entity);
    }

    @Override
    public LogDTO update(LogDTO dto) {
        log.debug("Request to update Log : {}", dto);
        LogEntity entity = logMapper.toEntity(dto);
        entity = logRepository.save(entity);
        return logMapper.toDto(entity);
    }

    @Override
    public Optional<LogDTO> partialUpdate(LogDTO dto) {
        log.debug("Request to partially update Log : {}", dto);
        return logRepository
            .findById(dto.getId())
            .map(existing -> {
                logMapper.partialUpdate(existing, dto);
                return existing;
            })
            .map(logRepository::save)
            .map(logMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LogDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Logs");
        return logRepository.findAll(pageable).map(logMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LogDTO> findOne(Long id) {
        log.debug("Request to get Log : {}", id);
        return logRepository.findById(id).map(logMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Log : {}", id);
        logRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LogListRowDTO> findSummary(Pageable pageable) {
        log.debug("Request to get summary page of Logs");
        return logRepository.findSummary(pageable)
            .map(row -> new LogListRowDTO(
                row.getId(),
                row.getMsgType(),
                row.getCorrelationId(),
                row.getReferenceType(),
                row.getReference(),
                row.getModuleSource(),
                row.getModuleDestination(),
                row.getProperties(),
                row.getReqMessage(),
                row.getResMessage(),
                row.getError(),
                row.getErrorDetails(),
                row.getInitialDate(),
                row.getCreateDate(),
                row.getLogType()
            ));
    }
}
