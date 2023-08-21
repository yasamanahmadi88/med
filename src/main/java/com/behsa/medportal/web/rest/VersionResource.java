package com.behsa.medportal.web.rest;

import com.behsa.medportal.domain.VersionEntity;
import com.behsa.medportal.repository.VersionRepository;
import com.behsa.medportal.service.LoggerService;
import com.behsa.medportal.service.VersionQueryService;
import com.behsa.medportal.service.VersionService;
import com.behsa.medportal.service.criteria.VersionCriteria;
import com.behsa.medportal.service.dto.VersionDTO;
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

/**
 * REST controller for managing {@link VersionEntity}.
 */
@RestController
@RequestMapping("/api")
public class VersionResource {

    private final Logger log = LoggerFactory.getLogger(VersionResource.class);

    private static final String ENTITY_NAME = "version";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final VersionService versionService;

    private final VersionRepository versionRepository;

    private final VersionQueryService versionQueryService;

    private final LoggerService loggerService;

    public VersionResource(VersionService versionService, VersionRepository versionRepository, VersionQueryService versionQueryService, LoggerService loggerService) {
        this.versionService = versionService;
        this.versionRepository = versionRepository;
        this.versionQueryService = versionQueryService;
        this.loggerService = loggerService;
    }

    /**
     * {@code POST  /versions} : Create a new version.
     *
     * @param versionDTO the versionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new versionDTO, or with status {@code 400 (Bad Request)} if the version has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/versions")
    @Secured(ENTITY_NAME)
    public ResponseEntity<VersionDTO> createVersion(@Valid @RequestBody VersionDTO versionDTO) throws URISyntaxException {
        log.debug("REST request to save Version : {}", versionDTO);
        if (versionDTO.getId() != null) {
            throw new BadRequestAlertException("A new version cannot already have an ID", ENTITY_NAME, "idexists");
        }
        VersionDTO result = versionService.save(versionDTO);
        loggerService.log( ENTITY_NAME+"_CREATE",new HashMap<>());
        return ResponseEntity
            .created(new URI("/api/versions/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /versions/:id} : Updates an existing version.
     *
     * @param id the id of the versionDTO to save.
     * @param versionDTO the versionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated versionDTO,
     * or with status {@code 400 (Bad Request)} if the versionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the versionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/versions/{id}")
    @Secured(ENTITY_NAME)
    public ResponseEntity<VersionDTO> updateVersion(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody VersionDTO versionDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Version : {}, {}", id, versionDTO);
        if (versionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, versionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!versionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        VersionDTO result = versionService.update(versionDTO);
        loggerService.log( ENTITY_NAME+"_UPDATE",new HashMap<>());
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, versionDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /versions/:id} : Partial updates given fields of an existing version, field will ignore if it is null
     *
     * @param id the id of the versionDTO to save.
     * @param versionDTO the versionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated versionDTO,
     * or with status {@code 400 (Bad Request)} if the versionDTO is not valid,
     * or with status {@code 404 (Not Found)} if the versionDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the versionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/versions/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @Secured(ENTITY_NAME)
    public ResponseEntity<VersionDTO> partialUpdateVersion(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody VersionDTO versionDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Version partially : {}, {}", id, versionDTO);
        if (versionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, versionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!versionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<VersionDTO> result = versionService.partialUpdate(versionDTO);
        loggerService.log( ENTITY_NAME+"_UPDATE",new HashMap<>());
        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, versionDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /versions} : get all the versions.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of versions in body.
     */
    @GetMapping("/versions")
    @Secured(ENTITY_NAME)
    public ResponseEntity<List<VersionDTO>> getAllVersions(
        VersionCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get Versions by criteria: {}", criteria);
        Page<VersionDTO> page = versionQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /versions/count} : count all the versions.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/versions/count")
    @Secured(ENTITY_NAME)
    public ResponseEntity<Long> countVersions(VersionCriteria criteria) {
        log.debug("REST request to count Versions by criteria: {}", criteria);
        return ResponseEntity.ok().body(versionQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /versions/:id} : get the "id" version.
     *
     * @param id the id of the versionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the versionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/versions/{id}")
    @Secured(ENTITY_NAME)
    public ResponseEntity<VersionDTO> getVersion(@PathVariable Long id) {
        log.debug("REST request to get Version : {}", id);
        Optional<VersionDTO> versionDTO = versionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(versionDTO);
    }

    /**
     * {@code DELETE  /versions/:id} : delete the "id" version.
     *
     * @param id the id of the versionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/versions/{id}")
    @Secured(ENTITY_NAME)
    public ResponseEntity<Void> deleteVersion(@PathVariable Long id) {
        log.debug("REST request to delete Version : {}", id);
        versionService.delete(id);
        loggerService.log( ENTITY_NAME+"_DELETE",new HashMap<>());
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
