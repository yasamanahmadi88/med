package com.behsa.medportal.web.rest;

import com.behsa.medportal.domain.InstanceEntity;
import com.behsa.medportal.repository.InstanceRepository;
import com.behsa.medportal.service.InstanceQueryService;
import com.behsa.medportal.service.InstanceService;
import com.behsa.medportal.service.criteria.InstanceCriteria;
import com.behsa.medportal.service.dto.InstanceDTO;
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
 * REST controller for managing {@link InstanceEntity}.
 */
@RestController
@RequestMapping("/api")
public class InstanceResource {

    private final Logger log = LoggerFactory.getLogger(InstanceResource.class);

    private static final String ENTITY_NAME = "instance";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final InstanceService instanceService;

    private final InstanceRepository instanceRepository;

    private final InstanceQueryService instanceQueryService;

    public InstanceResource(
        InstanceService instanceService,
        InstanceRepository instanceRepository,
        InstanceQueryService instanceQueryService
    ) {
        this.instanceService = instanceService;
        this.instanceRepository = instanceRepository;
        this.instanceQueryService = instanceQueryService;
    }

    /**
     * {@code POST  /instances} : Create a new instance.
     *
     * @param instanceDTO the instanceDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new instanceDTO, or with status {@code 400 (Bad Request)} if the instance has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/instances")
    @Secured(ENTITY_NAME)
    public ResponseEntity<InstanceDTO> createInstance(@Valid @RequestBody InstanceDTO instanceDTO) throws URISyntaxException {
        log.debug("REST request to save Instance : {}", instanceDTO);
        if (instanceDTO.getId() != null) {
            throw new BadRequestAlertException("A new instance cannot already have an ID", ENTITY_NAME, "idexists");
        }
        InstanceDTO result = instanceService.save(instanceDTO);
        return ResponseEntity
            .created(new URI("/api/instances/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /instances/:id} : Updates an existing instance.
     *
     * @param id the id of the instanceDTO to save.
     * @param instanceDTO the instanceDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated instanceDTO,
     * or with status {@code 400 (Bad Request)} if the instanceDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the instanceDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/instances/{id}")
    @Secured(ENTITY_NAME)
    public ResponseEntity<InstanceDTO> updateInstance(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody InstanceDTO instanceDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Instance : {}, {}", id, instanceDTO);
        if (instanceDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, instanceDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!instanceRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        InstanceDTO result = instanceService.update(instanceDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, instanceDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /instances/:id} : Partial updates given fields of an existing instance, field will ignore if it is null
     *
     * @param id the id of the instanceDTO to save.
     * @param instanceDTO the instanceDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated instanceDTO,
     * or with status {@code 400 (Bad Request)} if the instanceDTO is not valid,
     * or with status {@code 404 (Not Found)} if the instanceDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the instanceDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/instances/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @Secured(ENTITY_NAME)
    public ResponseEntity<InstanceDTO> partialUpdateInstance(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody InstanceDTO instanceDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Instance partially : {}, {}", id, instanceDTO);
        if (instanceDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, instanceDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!instanceRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<InstanceDTO> result = instanceService.partialUpdate(instanceDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, instanceDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /instances} : get all the instances.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of instances in body.
     */
    @GetMapping("/instances")
    @Secured(ENTITY_NAME)
    public ResponseEntity<List<InstanceDTO>> getAllInstances(
        InstanceCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get Instances by criteria: {}", criteria);
        Page<InstanceDTO> page = instanceQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /instances/count} : count all the instances.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/instances/count")
    @Secured(ENTITY_NAME)
    public ResponseEntity<Long> countInstances(InstanceCriteria criteria) {
        log.debug("REST request to count Instances by criteria: {}", criteria);
        return ResponseEntity.ok().body(instanceQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /instances/:id} : get the "id" instance.
     *
     * @param id the id of the instanceDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the instanceDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/instances/{id}")
    @Secured(ENTITY_NAME)
    public ResponseEntity<InstanceDTO> getInstance(@PathVariable Long id) {
        log.debug("REST request to get Instance : {}", id);
        Optional<InstanceDTO> instanceDTO = instanceService.findOne(id);
        return ResponseUtil.wrapOrNotFound(instanceDTO);
    }

    /**
     * {@code DELETE  /instances/:id} : delete the "id" instance.
     *
     * @param id the id of the instanceDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/instances/{id}")
    @Secured(ENTITY_NAME)
    public ResponseEntity<Void> deleteInstance(@PathVariable Long id) {
        log.debug("REST request to delete Instance : {}", id);
        instanceService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
