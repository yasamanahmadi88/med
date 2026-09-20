package com.behsa.medportal.service;

import com.behsa.medportal.service.dto.LogDTO;
import com.behsa.medportal.service.dto.LogListRowDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LogService {
    LogDTO save(LogDTO dto);
    LogDTO update(LogDTO dto);
    Optional<LogDTO> partialUpdate(LogDTO dto);
    Page<LogDTO> findAll(Pageable pageable);
    Optional<LogDTO> findOne(Long id);
    void delete(Long id);

    // summary projection page
    Page<LogListRowDTO> findSummary(Pageable pageable);
}
