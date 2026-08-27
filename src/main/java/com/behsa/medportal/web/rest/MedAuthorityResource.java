package com.behsa.medportal.web.rest;

import com.behsa.medportal.repository.MedAuthorityRepository;
import com.behsa.medportal.service.MedAuthorityQueryService;
import com.behsa.medportal.service.MedAuthorityService;
import com.behsa.medportal.service.criteria.MedAuthorityCriteria;
import com.behsa.medportal.service.dto.MedAuthorityDTO;
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
 * REST controller for managing {@link com.behsa.medportal.domain.MedAuthorityEntity}.
 */
@RestController
@RequestMapping("/api")
public class MedAuthorityResource {

    private final Logger log = LoggerFactory.getLogger(MedAuthorityResource.class);

    private static final String ENTITY_NAME = "medAuthority";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final MedAuthorityService medAuthorityService;

    private final MedAuthorityRepository medAuthorityRepository;

    private final MedAuthorityQueryService medAuthorityQueryService;

    public MedAuthorityResource(
        MedAuthorityService medAuthorityService,
        MedAuthorityRepository medAuthorityRepository,
        MedAuthorityQueryService medAuthorityQueryService
    ) {
        this.medAuthorityService = medAuthorityService;
        this.medAuthorityRepository = medAuthorityRepository;
        this.medAuthorityQueryService = medAuthorityQueryService;
    }

    /**
     * {@code POST  /med-authorities} : Create a new medAuthority.
     *
     * @param medAuthorityDTO the medAuthorityDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new medAuthorityDTO, or with status {@code 400 (Bad Request)} if the medAuthority has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/med-authorities")
    @Secured(ENTITY_NAME)
    public ResponseEntity<MedAuthorityDTO> createMedAuthority(@Valid @RequestBody MedAuthorityDTO medAuthorityDTO)
        throws URISyntaxException {
        log.debug("REST request to save MedAuthority : {}", medAuthorityDTO);
        if (medAuthorityDTO.getId() != null) {
            throw new BadRequestAlertException("A new medAuthority cannot already have an ID", ENTITY_NAME, "idexists");
        }
        MedAuthorityDTO result = medAuthorityService.save(medAuthorityDTO);
        return ResponseEntity
            .created(new URI("/api/med-authorities/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /med-authorities/:id} : Updates an existing medAuthority.
     *
     * @param id the id of the medAuthorityDTO to save.
     * @param medAuthorityDTO the medAuthorityDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated medAuthorityDTO,
     * or with status {@code 400 (Bad Request)} if the medAuthorityDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the medAuthorityDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/med-authorities/{id}")
    @Secured(ENTITY_NAME)
    public ResponseEntity<MedAuthorityDTO> updateMedAuthority(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody MedAuthorityDTO medAuthorityDTO
    ) throws URISyntaxException {
        log.debug("REST request to update MedAuthority : {}, {}", id, medAuthorityDTO);
        if (medAuthorityDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, medAuthorityDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!medAuthorityRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        MedAuthorityDTO result = medAuthorityService.update(medAuthorityDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, medAuthorityDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /med-authorities/:id} : Partial updates given fields of an existing medAuthority, field will ignore if it is null
     *
     * @param id the id of the medAuthorityDTO to save.
     * @param medAuthorityDTO the medAuthorityDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated medAuthorityDTO,
     * or with status {@code 400 (Bad Request)} if the medAuthorityDTO is not valid,
     * or with status {@code 404 (Not Found)} if the medAuthorityDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the medAuthorityDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/med-authorities/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @Secured(ENTITY_NAME)
    public ResponseEntity<MedAuthorityDTO> partialUpdateMedAuthority(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody MedAuthorityDTO medAuthorityDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update MedAuthority partially : {}, {}", id, medAuthorityDTO);
        if (medAuthorityDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, medAuthorityDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!medAuthorityRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<MedAuthorityDTO> result = medAuthorityService.partialUpdate(medAuthorityDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, medAuthorityDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /med-authorities} : get all the medAuthorities.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of medAuthorities in body.
     */
    @GetMapping("/med-authorities")
    @Secured(ENTITY_NAME)
    public ResponseEntity<List<MedAuthorityDTO>> getAllMedAuthorities(
        MedAuthorityCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get MedAuthorities by criteria: {}", criteria);
        Page<MedAuthorityDTO> page = medAuthorityQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /med-authorities/count} : count all the medAuthorities.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/med-authorities/count")
    @Secured(ENTITY_NAME)
    public ResponseEntity<Long> countMedAuthorities(MedAuthorityCriteria criteria) {
        log.debug("REST request to count MedAuthorities by criteria: {}", criteria);
        return ResponseEntity.ok().body(medAuthorityQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /med-authorities/:id} : get the "id" medAuthority.
     *
     * @param id the id of the medAuthorityDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the medAuthorityDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/med-authorities/{id}")
    @Secured(ENTITY_NAME)
    public ResponseEntity<MedAuthorityDTO> getMedAuthority(@PathVariable Long id) {
        log.debug("REST request to get MedAuthority : {}", id);
        Optional<MedAuthorityDTO> medAuthorityDTO = medAuthorityService.findOne(id);
        return ResponseUtil.wrapOrNotFound(medAuthorityDTO);
    }

    /**
     * {@code DELETE  /med-authorities/:id} : delete the "id" medAuthority.
     *
     * @param id the id of the medAuthorityDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/med-authorities/{id}")
    @Secured(ENTITY_NAME)
    public ResponseEntity<Void> deleteMedAuthority(@PathVariable Long id) {
        log.debug("REST request to delete MedAuthority : {}", id);
        medAuthorityService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
