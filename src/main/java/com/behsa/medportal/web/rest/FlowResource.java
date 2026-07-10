package com.behsa.medportal.web.rest;

import com.behsa.medportal.domain.FlowEntity;
import com.behsa.medportal.repository.FlowRepository;
import com.behsa.medportal.service.FlowQueryService;
import com.behsa.medportal.service.FlowService;
import com.behsa.medportal.service.LoggerService;
import com.behsa.medportal.service.criteria.FlowCriteria;
import com.behsa.medportal.service.dto.FlowDTO;
import com.behsa.medportal.service.mapper.FlowMapper;
import com.behsa.medportal.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
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

    @Value(value = "${mediation.bpmn.parser.url}")
    private String bpmnParserUrl;

    @Value(value = "${mediation.bpmn.parser.active}")
    private boolean bpmnParserActive;

    @Autowired
    public RestTemplate restTemplate;

    private final Logger log = LoggerFactory.getLogger(FlowResource.class);

    private static final String ENTITY_NAME = "flow";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final FlowService flowService;

    private final FlowRepository flowRepository;

    private final FlowQueryService flowQueryService;

    private final LoggerService loggerService;

    private final FlowMapper flowMapper;

    public FlowResource(FlowService flowService, FlowRepository flowRepository, FlowQueryService flowQueryService, LoggerService loggerService, FlowMapper flowMapper) {
        this.flowService = flowService;
        this.flowRepository = flowRepository;
        this.flowQueryService = flowQueryService;
        this.loggerService = loggerService;
        this.flowMapper = flowMapper;
    }

    /**
     * {@code POST  /flows} : Create a new flow.
     *
     * @param flowDTO the flowDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new flowDTO, or with status {@code 400 (Bad Request)} if the flow has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/flows")
    @Secured(ENTITY_NAME)
    public ResponseEntity<Object> createFlow(@Valid @RequestBody FlowDTO flowDTO) throws Exception {
        log.debug("REST request to save Flow : {}", flowDTO);
        if (flowDTO.getId() != null) {
            throw new BadRequestAlertException("A new flow cannot already have an ID", ENTITY_NAME, "idexists");
        }
        if (bpmnParserActive) {
            return sendToBpmnParser(flowDTO, "create", bpmnParserUrl);
        }
        FlowDTO result = flowService.save(flowDTO);
        loggerService.log( ENTITY_NAME+"_CREATE",new HashMap<>());
        return ResponseEntity
            .created(new URI("/api/flows/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    public ResponseEntity<Object> sendToBpmnParser(FlowDTO flowDTO, String bpmnType, String bpmnParserUrl) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/xml"));
        headers.set("FLOW_NAME", flowDTO.getFlowName());
        headers.set("FLOW_DESC", flowDTO.getFlowDesc());
        headers.set("PRODUCT_NAME", flowDTO.getProduct().getProductName());
        HttpEntity<String> requestEntity = new HttpEntity<>(flowDTO.getFlow(), headers);
        return restTemplate.postForEntity(bpmnParserUrl + "/" + bpmnType, requestEntity, Object.class);
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
    @Secured(ENTITY_NAME)
    public ResponseEntity<Object> updateFlow(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody FlowDTO flowDTO
    ) throws Exception {
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
        if (!bpmnParserActive) {
        FlowDTO result = flowService.update(flowDTO);
        loggerService.log( ENTITY_NAME+"_UPDATE",new HashMap<>());
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, flowDTO.getId().toString()))
            .body(result);
        }
        return sendToBpmnParser(flowDTO, "update", bpmnParserUrl);
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
    @Secured(ENTITY_NAME)
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
        loggerService.log( ENTITY_NAME+"_UPDATE",new HashMap<>());
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
    @Secured(ENTITY_NAME)
    public ResponseEntity<List<FlowDTO>> getAllFlows(
        FlowCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
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
    @Secured(ENTITY_NAME)
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
    @Secured(ENTITY_NAME)
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
    @Secured(ENTITY_NAME)
    public ResponseEntity<Void> deleteFlow(@PathVariable Long id) {
        log.debug("REST request to delete Flow : {}", id);
        flowService.delete(id);
        loggerService.log( ENTITY_NAME+"_DELETE",new HashMap<>());
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    @PostMapping("/flows/save")
    @Secured(ENTITY_NAME)
    public ResponseEntity<Map> saveFlow(@RequestBody String flow) throws URISyntaxException {

        //TODO send e.data to api provided by mediation team

        Map<String, String> map = new HashMap<>();
        map.put("mediationStatus", "success"); //TODO response received from mediation

        return ResponseEntity
            .ok()
            .body(map);
    }

    @PostMapping("/flows/isFlowNameValid")
    @Secured(ENTITY_NAME)
    public ResponseEntity<List<FlowDTO>> isFlowNameValid(@RequestBody String flowName) throws URISyntaxException {

        List<FlowDTO> flowDTOS = flowMapper.toDto(flowRepository.findAllByFlowNameStartsWith(flowName + "_"));

        return ResponseEntity
            .ok()
            .body(flowDTOS);
    }
}
