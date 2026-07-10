package com.behsa.medportal.web.rest;

import com.behsa.medportal.domain.LogEntity;
import com.behsa.medportal.repository.LogRepository;
import com.behsa.medportal.service.LogQueryService;
import com.behsa.medportal.service.LogService;
import com.behsa.medportal.service.LoggerService;
import com.behsa.medportal.service.criteria.LogCriteria;
import com.behsa.medportal.service.dto.LogDTO;
import com.behsa.medportal.service.dto.LogListRowDTO;
import com.behsa.medportal.service.dto.ProductDTO;
import com.behsa.medportal.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

@RestController
@RequestMapping("/api")

//TODO COMMENT SECURED

public class LogResource {

    private final Logger log = LoggerFactory.getLogger(LogResource.class);
    private static final String ENTITY_NAME = "log";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final LogService logService;
    private final LogRepository logRepository;
    private final LogQueryService logQueryService;
    private final LoggerService loggerService;

    public LogResource(LogService logService, LogRepository logRepository, LogQueryService logQueryService, LoggerService loggerService) {
        this.logService = logService;
        this.logRepository = logRepository;
        this.logQueryService = logQueryService;
        this.loggerService = loggerService;
    }

    @PostMapping("/logs")
    @Secured(ENTITY_NAME)
    public ResponseEntity<LogDTO> createLog(@Valid @RequestBody LogDTO logDTO) throws URISyntaxException {
        log.debug("REST request to save Log : {}", logDTO);
        if (logDTO.getId() != null) {
            throw new BadRequestAlertException("A new log cannot already have an ID", ENTITY_NAME, "idexists");
        }
        LogDTO result = logService.save(logDTO);
        loggerService.log(ENTITY_NAME + "_CREATE", new HashMap<>());
        return ResponseEntity
            .created(new URI("/api/logs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    @PutMapping("/logs/{id}")
    @Secured(ENTITY_NAME)
    public ResponseEntity<LogDTO> updateLog(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody LogDTO logDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Log : {}, {}", id, logDTO);
        if (logDTO.getId() == null) throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        if (!Objects.equals(id, logDTO.getId())) throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        if (!logRepository.existsById(id)) throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");

        LogDTO result = logService.update(logDTO);
        loggerService.log(ENTITY_NAME + "_UPDATE", new HashMap<>());
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, logDTO.getId().toString()))
            .body(result);
    }

    @PatchMapping(value = "/logs/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @Secured(ENTITY_NAME)
    public ResponseEntity<LogDTO> partialUpdateLog(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody LogDTO logDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Log : {}, {}", id, logDTO);
        if (logDTO.getId() == null) throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        if (!Objects.equals(id, logDTO.getId())) throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        if (!logRepository.existsById(id)) throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");

        Optional<LogDTO> result = logService.partialUpdate(logDTO);
        loggerService.log(ENTITY_NAME + "_UPDATE", new HashMap<>());
        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, logDTO.getId().toString())
        );
    }

    @GetMapping("/logs")
    @Secured(ENTITY_NAME)
    public ResponseEntity<List<LogDTO>> getAllLogs(
        LogCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get Logs by criteria: {}", criteria);
        Page<LogDTO> page = logQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/logs/count")
    @Secured(ENTITY_NAME)
    public ResponseEntity<Long> countLogs(LogCriteria criteria) {
        log.debug("REST request to count Logs by criteria: {}", criteria);
        return ResponseEntity.ok().body(logQueryService.countByCriteria(criteria));
    }

    @GetMapping("/logs/{id}")
    @Secured(ENTITY_NAME)
    public ResponseEntity<LogDTO> getLog(@PathVariable Long id) {
        log.debug("REST request to get Log : {}", id);
        Optional<LogDTO> dto = logService.findOne(id);
        return ResponseUtil.wrapOrNotFound(dto);
    }

    @DeleteMapping("/logs/{id}")
    @Secured(ENTITY_NAME)
    public ResponseEntity<Void> deleteLog(@PathVariable Long id) {
        log.debug("REST request to delete Log : {}", id);
        logService.delete(id);
        loggerService.log(ENTITY_NAME + "_DELETE", new HashMap<>());
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * Your requested projection:
     * select MSG_TYPE, CORRELATION_ID, REFERENCE_TYPE, REFERENCE, MODULE_SOURCE,
     * MODULE_DESTINATION, PROPERTIES, REQ_MESSAGE, RES_MESSAGE, ERROR, ERROR_DETAILS,
     * INITIAL_DATE, CREATE_DATE, LOG_TYPE
     * ordered by CREATE_DATE desc, paged.
     */
    @GetMapping("/logs/summary")
    @Secured(ENTITY_NAME)
    public ResponseEntity<List<LogListRowDTO>> getLogsSummary(@org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get Logs summary page");
        Page<LogListRowDTO> page = logService.findSummary(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/logs/search")
    @Secured(ENTITY_NAME)
    public ResponseEntity<List<LogDTO>> searchLogsByCorrelationId(
        @RequestParam String searchText,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to search Logs by correlationId: {}", searchText);
        Page<LogDTO> page = logQueryService.searchByCorrelationId(searchText, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
