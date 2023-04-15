package com.behsa.medportal.web.rest;

import com.behsa.medportal.med.domain.FlowEntity;
import com.behsa.medportal.med.repository.FlowRepository;
import com.behsa.medportal.service.FlowQueryService;
import com.behsa.medportal.service.FlowService;
import com.behsa.medportal.service.criteria.FlowCriteria;
import com.behsa.medportal.service.dto.FlowDTO;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link FlowEntity}.
 */
@RestController
@RequestMapping("/api")
public class FlowResource {

    private final Logger log = LoggerFactory.getLogger(FlowResource.class);

    private static final String ENTITY_NAME = "flow";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final FlowService flowService;

    private final FlowRepository flowRepository;

    private final FlowQueryService flowQueryService;

    public FlowResource(FlowService flowService, FlowRepository flowRepository, FlowQueryService flowQueryService) {
        this.flowService = flowService;
        this.flowRepository = flowRepository;
        this.flowQueryService = flowQueryService;
    }

    /**
     * {@code POST  /flows} : Create a new flow.
     *
     * @param flowDTO the flowDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new flowDTO, or with status {@code 400 (Bad Request)} if the flow has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/flows")
    public ResponseEntity<FlowDTO> createFlow(@Valid @RequestBody FlowDTO flowDTO) throws URISyntaxException {
        log.debug("REST request to save Flow : {}", flowDTO);
        if (flowDTO.getId() != null) {
            throw new BadRequestAlertException("A new flow cannot already have an ID", ENTITY_NAME, "idexists");
        }
        FlowDTO result = flowService.save(flowDTO);
        return ResponseEntity
            .created(new URI("/api/flows/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /flows/:id} : Updates an existing flow.
     *
     * @param id the id of the flowDTO to save.
     * @param flowDTO the flowDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated flowDTO,
     * or with status {@code 400 (Bad Request)} if the flowDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the flowDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/flows/{id}")
    public ResponseEntity<FlowDTO> updateFlow(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody FlowDTO flowDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Flow : {}, {}", id, flowDTO);
        if (flowDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, flowDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!flowRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        FlowDTO result = flowService.update(flowDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, flowDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /flows/:id} : Partial updates given fields of an existing flow, field will ignore if it is null
     *
     * @param id the id of the flowDTO to save.
     * @param flowDTO the flowDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated flowDTO,
     * or with status {@code 400 (Bad Request)} if the flowDTO is not valid,
     * or with status {@code 404 (Not Found)} if the flowDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the flowDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/flows/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<FlowDTO> partialUpdateFlow(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody FlowDTO flowDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Flow partially : {}, {}", id, flowDTO);
        if (flowDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, flowDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!flowRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<FlowDTO> result = flowService.partialUpdate(flowDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, flowDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /flows} : get all the flows.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of flows in body.
     */
    @GetMapping("/flows")
    public ResponseEntity<List<FlowDTO>> getAllFlows(
        FlowCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get Flows by criteria: {}", criteria);
        Page<FlowDTO> page = flowQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /flows/count} : count all the flows.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/flows/count")
    public ResponseEntity<Long> countFlows(FlowCriteria criteria) {
        log.debug("REST request to count Flows by criteria: {}", criteria);
        return ResponseEntity.ok().body(flowQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /flows/:id} : get the "id" flow.
     *
     * @param id the id of the flowDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the flowDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/flows/{id}")
    public ResponseEntity<FlowDTO> getFlow(@PathVariable Long id) {
        log.debug("REST request to get Flow : {}", id);
        Optional<FlowDTO> flowDTO = flowService.findOne(id);
        return ResponseUtil.wrapOrNotFound(flowDTO);
    }

    /**
     * {@code DELETE  /flows/:id} : delete the "id" flow.
     *
     * @param id the id of the flowDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/flows/{id}")
    public ResponseEntity<Void> deleteFlow(@PathVariable Long id) {
        log.debug("REST request to delete Flow : {}", id);
        flowService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
