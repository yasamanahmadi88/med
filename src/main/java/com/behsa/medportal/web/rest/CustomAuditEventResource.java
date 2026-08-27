package com.behsa.medportal.web.rest;

import com.behsa.medportal.repository.CustomAuditEventRepository;
import com.behsa.medportal.service.CustomAuditEventQueryService;
import com.behsa.medportal.service.CustomAuditEventService;
import com.behsa.medportal.service.criteria.CustomAuditEventCriteria;
import com.behsa.medportal.service.dto.CustomAuditEventDTO;
import com.behsa.medportal.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
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

/**
 * REST controller for managing {@link com.behsa.medportal.domain.CustomAuditEventEntity}.
 */
@RestController
@RequestMapping("/api")
public class CustomAuditEventResource {

    private final Logger log = LoggerFactory.getLogger(CustomAuditEventResource.class);

    private static final String ENTITY_NAME = "customAuditEvent";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CustomAuditEventService customAuditEventService;

    private final CustomAuditEventRepository customAuditEventRepository;

    private final CustomAuditEventQueryService customAuditEventQueryService;

    public CustomAuditEventResource(
        CustomAuditEventService customAuditEventService,
        CustomAuditEventRepository customAuditEventRepository,
        CustomAuditEventQueryService customAuditEventQueryService
    ) {
        this.customAuditEventService = customAuditEventService;
        this.customAuditEventRepository = customAuditEventRepository;
        this.customAuditEventQueryService = customAuditEventQueryService;
    }

    /**
     * {@code POST  /custom-audit-events} : Create a new customAuditEvent.
     *
     * @param customAuditEventDTO the customAuditEventDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new customAuditEventDTO, or with status {@code 400 (Bad Request)} if the customAuditEvent has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/custom-audit-events")
    @Secured(ENTITY_NAME)
    public ResponseEntity<CustomAuditEventDTO> createCustomAuditEvent(@Valid @RequestBody CustomAuditEventDTO customAuditEventDTO)
        throws URISyntaxException {
        log.debug("REST request to save CustomAuditEvent : {}", customAuditEventDTO);
        if (customAuditEventDTO.getId() != null) {
            throw new BadRequestAlertException("A new customAuditEvent cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CustomAuditEventDTO result = customAuditEventService.save(customAuditEventDTO);
        return ResponseEntity
            .created(new URI("/api/custom-audit-events/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /custom-audit-events/:id} : Updates an existing customAuditEvent.
     *
     * @param id the id of the customAuditEventDTO to save.
     * @param customAuditEventDTO the customAuditEventDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated customAuditEventDTO,
     * or with status {@code 400 (Bad Request)} if the customAuditEventDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the customAuditEventDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/custom-audit-events/{id}")
    @Secured(ENTITY_NAME)
    public ResponseEntity<CustomAuditEventDTO> updateCustomAuditEvent(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CustomAuditEventDTO customAuditEventDTO
    ) throws URISyntaxException {
        log.debug("REST request to update CustomAuditEvent : {}, {}", id, customAuditEventDTO);
        if (customAuditEventDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, customAuditEventDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!customAuditEventRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        CustomAuditEventDTO result = customAuditEventService.update(customAuditEventDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, customAuditEventDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /custom-audit-events/:id} : Partial updates given fields of an existing customAuditEvent, field will ignore if it is null
     *
     * @param id the id of the customAuditEventDTO to save.
     * @param customAuditEventDTO the customAuditEventDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated customAuditEventDTO,
     * or with status {@code 400 (Bad Request)} if the customAuditEventDTO is not valid,
     * or with status {@code 404 (Not Found)} if the customAuditEventDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the customAuditEventDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/custom-audit-events/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @Secured(ENTITY_NAME)
    public ResponseEntity<CustomAuditEventDTO> partialUpdateCustomAuditEvent(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody CustomAuditEventDTO customAuditEventDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update CustomAuditEvent partially : {}, {}", id, customAuditEventDTO);
        if (customAuditEventDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, customAuditEventDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!customAuditEventRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<CustomAuditEventDTO> result = customAuditEventService.partialUpdate(customAuditEventDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, customAuditEventDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /custom-audit-events} : get all the customAuditEvents.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of customAuditEvents in body.
     */
    @GetMapping("/custom-audit-events")
    @Secured(ENTITY_NAME)
    public ResponseEntity<List<CustomAuditEventDTO>> getAllCustomAuditEvents(
        CustomAuditEventCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get CustomAuditEvents by criteria: {}", criteria);
        Page<CustomAuditEventDTO> page = customAuditEventQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /custom-audit-events/count} : count all the customAuditEvents.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/custom-audit-events/count")
    @Secured(ENTITY_NAME)
    public ResponseEntity<Long> countCustomAuditEvents(CustomAuditEventCriteria criteria) {
        log.debug("REST request to count CustomAuditEvents by criteria: {}", criteria);
        return ResponseEntity.ok().body(customAuditEventQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /custom-audit-events/:id} : get the "id" customAuditEvent.
     *
     * @param id the id of the customAuditEventDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the customAuditEventDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/custom-audit-events/{id}")
    @Secured(ENTITY_NAME)
    public ResponseEntity<CustomAuditEventDTO> getCustomAuditEvent(@PathVariable Long id) {
        log.debug("REST request to get CustomAuditEvent : {}", id);
        Optional<CustomAuditEventDTO> customAuditEventDTO = customAuditEventService.findOne(id);
        return ResponseUtil.wrapOrNotFound(customAuditEventDTO);
    }

    /**
     * {@code DELETE  /custom-audit-events/:id} : delete the "id" customAuditEvent.
     *
     * @param id the id of the customAuditEventDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/custom-audit-events/{id}")
    @Secured(ENTITY_NAME)
    public ResponseEntity<Void> deleteCustomAuditEvent(@PathVariable Long id) {
        log.debug("REST request to delete CustomAuditEvent : {}", id);
        customAuditEventService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
