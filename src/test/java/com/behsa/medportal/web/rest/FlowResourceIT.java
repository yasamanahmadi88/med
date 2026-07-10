package com.behsa.medportal.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.behsa.medportal.IntegrationTest;
import com.behsa.medportal.domain.FlowEntity;
import com.behsa.medportal.domain.ProductEntity;
import com.behsa.medportal.repository.FlowRepository;
import com.behsa.medportal.service.dto.FlowDTO;
import com.behsa.medportal.service.mapper.FlowMapper;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link FlowResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class FlowResourceIT {

    private static final String DEFAULT_FLOW_NAME = "AAAAAA";
    private static final String UPDATED_FLOW_NAME = "BBBBBB";

    private static final String DEFAULT_FLOW_DESC = "AAAAAAAAAA";
    private static final String UPDATED_FLOW_DESC = "BBBBBBBBBB";

    private static final String DEFAULT_FLOW = "AAAAAAAAAA";
    private static final String UPDATED_FLOW = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/flows";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private FlowRepository flowRepository;

    @Autowired
    private FlowMapper flowMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restFlowMockMvc;

    private FlowEntity flowEntity;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static FlowEntity createEntity(EntityManager em) {
        FlowEntity flowEntity = new FlowEntity().flowName(DEFAULT_FLOW_NAME).flowDesc(DEFAULT_FLOW_DESC).flow(DEFAULT_FLOW);
        // Add required entity
        ProductEntity product;
        if (TestUtil.findAll(em, ProductEntity.class).isEmpty()) {
            product = ProductResourceIT.createEntity(em);
            em.persist(product);
            em.flush();
        } else {
            product = TestUtil.findAll(em, ProductEntity.class).get(0);
        }
        flowEntity.setProduct(product);
        return flowEntity;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static FlowEntity createUpdatedEntity(EntityManager em) {
        FlowEntity flowEntity = new FlowEntity().flowName(UPDATED_FLOW_NAME).flowDesc(UPDATED_FLOW_DESC).flow(UPDATED_FLOW);
        // Add required entity
        ProductEntity product;
        if (TestUtil.findAll(em, ProductEntity.class).isEmpty()) {
            product = ProductResourceIT.createUpdatedEntity(em);
            em.persist(product);
            em.flush();
        } else {
            product = TestUtil.findAll(em, ProductEntity.class).get(0);
        }
        flowEntity.setProduct(product);
        return flowEntity;
    }

    @BeforeEach
    public void initTest() {
        flowEntity = createEntity(em);
    }

    @Test
    @Transactional
    void createFlow() throws Exception {
        int databaseSizeBeforeCreate = flowRepository.findAll().size();
        // Create the Flow
        FlowDTO flowDTO = flowMapper.toDto(flowEntity);
        restFlowMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(flowDTO)))
            .andExpect(status().isCreated());

        // Validate the Flow in the database
        List<FlowEntity> flowList = flowRepository.findAll();
        assertThat(flowList).hasSize(databaseSizeBeforeCreate + 1);
        FlowEntity testFlow = flowList.get(flowList.size() - 1);
        assertThat(testFlow.getFlowName()).isEqualTo(DEFAULT_FLOW_NAME);
        assertThat(testFlow.getFlowDesc()).isEqualTo(DEFAULT_FLOW_DESC);
        assertThat(testFlow.getFlow()).isEqualTo(DEFAULT_FLOW);
    }

    @Test
    @Transactional
    void createFlowWithExistingId() throws Exception {
        // Create the Flow with an existing ID
        flowEntity.setId(1L);
        FlowDTO flowDTO = flowMapper.toDto(flowEntity);

        int databaseSizeBeforeCreate = flowRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restFlowMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(flowDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Flow in the database
        List<FlowEntity> flowList = flowRepository.findAll();
        assertThat(flowList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkFlowNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = flowRepository.findAll().size();
        // set the field null
        flowEntity.setFlowName(null);

        // Create the Flow, which fails.
        FlowDTO flowDTO = flowMapper.toDto(flowEntity);

        restFlowMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(flowDTO)))
            .andExpect(status().isBadRequest());

        List<FlowEntity> flowList = flowRepository.findAll();
        assertThat(flowList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkFlowDescIsRequired() throws Exception {
        int databaseSizeBeforeTest = flowRepository.findAll().size();
        // set the field null
        flowEntity.setFlowDesc(null);

        // Create the Flow, which fails.
        FlowDTO flowDTO = flowMapper.toDto(flowEntity);

        restFlowMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(flowDTO)))
            .andExpect(status().isBadRequest());

        List<FlowEntity> flowList = flowRepository.findAll();
        assertThat(flowList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkFlowIsRequired() throws Exception {
        int databaseSizeBeforeTest = flowRepository.findAll().size();
        // set the field null
        flowEntity.setFlow(null);

        // Create the Flow, which fails.
        FlowDTO flowDTO = flowMapper.toDto(flowEntity);

        restFlowMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(flowDTO)))
            .andExpect(status().isBadRequest());

        List<FlowEntity> flowList = flowRepository.findAll();
        assertThat(flowList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllFlows() throws Exception {
        // Initialize the database
        flowRepository.saveAndFlush(flowEntity);

        // Get all the flowList
        restFlowMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(flowEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].flowName").value(hasItem(DEFAULT_FLOW_NAME)))
            .andExpect(jsonPath("$.[*].flowDesc").value(hasItem(DEFAULT_FLOW_DESC)))
            .andExpect(jsonPath("$.[*].flow").value(hasItem(DEFAULT_FLOW)));
    }

    @Test
    @Transactional
    void getFlow() throws Exception {
        // Initialize the database
        flowRepository.saveAndFlush(flowEntity);

        // Get the flow
        restFlowMockMvc
            .perform(get(ENTITY_API_URL_ID, flowEntity.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(flowEntity.getId().intValue()))
            .andExpect(jsonPath("$.flowName").value(DEFAULT_FLOW_NAME))
            .andExpect(jsonPath("$.flowDesc").value(DEFAULT_FLOW_DESC))
            .andExpect(jsonPath("$.flow").value(DEFAULT_FLOW));
    }

    @Test
    @Transactional
    void getFlowsByIdFiltering() throws Exception {
        // Initialize the database
        flowRepository.saveAndFlush(flowEntity);

        Long id = flowEntity.getId();

        defaultFlowShouldBeFound("id.equals=" + id);
        defaultFlowShouldNotBeFound("id.notEquals=" + id);

        defaultFlowShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultFlowShouldNotBeFound("id.greaterThan=" + id);

        defaultFlowShouldBeFound("id.lessThanOrEqual=" + id);
        defaultFlowShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllFlowsByFlowNameIsEqualToSomething() throws Exception {
        // Initialize the database
        flowRepository.saveAndFlush(flowEntity);

        // Get all the flowList where flowName equals to DEFAULT_FLOW_NAME
        defaultFlowShouldBeFound("flowName.equals=" + DEFAULT_FLOW_NAME);

        // Get all the flowList where flowName equals to UPDATED_FLOW_NAME
        defaultFlowShouldNotBeFound("flowName.equals=" + UPDATED_FLOW_NAME);
    }

    @Test
    @Transactional
    void getAllFlowsByFlowNameIsInShouldWork() throws Exception {
        // Initialize the database
        flowRepository.saveAndFlush(flowEntity);

        // Get all the flowList where flowName in DEFAULT_FLOW_NAME or UPDATED_FLOW_NAME
        defaultFlowShouldBeFound("flowName.in=" + DEFAULT_FLOW_NAME + "," + UPDATED_FLOW_NAME);

        // Get all the flowList where flowName equals to UPDATED_FLOW_NAME
        defaultFlowShouldNotBeFound("flowName.in=" + UPDATED_FLOW_NAME);
    }

    @Test
    @Transactional
    void getAllFlowsByFlowNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        flowRepository.saveAndFlush(flowEntity);

        // Get all the flowList where flowName is not null
        defaultFlowShouldBeFound("flowName.specified=true");

        // Get all the flowList where flowName is null
        defaultFlowShouldNotBeFound("flowName.specified=false");
    }

    @Test
    @Transactional
    void getAllFlowsByFlowNameContainsSomething() throws Exception {
        // Initialize the database
        flowRepository.saveAndFlush(flowEntity);

        // Get all the flowList where flowName contains DEFAULT_FLOW_NAME
        defaultFlowShouldBeFound("flowName.contains=" + DEFAULT_FLOW_NAME);

        // Get all the flowList where flowName contains UPDATED_FLOW_NAME
        defaultFlowShouldNotBeFound("flowName.contains=" + UPDATED_FLOW_NAME);
    }

    @Test
    @Transactional
    void getAllFlowsByFlowNameNotContainsSomething() throws Exception {
        // Initialize the database
        flowRepository.saveAndFlush(flowEntity);

        // Get all the flowList where flowName does not contain DEFAULT_FLOW_NAME
        defaultFlowShouldNotBeFound("flowName.doesNotContain=" + DEFAULT_FLOW_NAME);

        // Get all the flowList where flowName does not contain UPDATED_FLOW_NAME
        defaultFlowShouldBeFound("flowName.doesNotContain=" + UPDATED_FLOW_NAME);
    }

    @Test
    @Transactional
    void getAllFlowsByFlowDescIsEqualToSomething() throws Exception {
        // Initialize the database
        flowRepository.saveAndFlush(flowEntity);

        // Get all the flowList where flowDesc equals to DEFAULT_FLOW_DESC
        defaultFlowShouldBeFound("flowDesc.equals=" + DEFAULT_FLOW_DESC);

        // Get all the flowList where flowDesc equals to UPDATED_FLOW_DESC
        defaultFlowShouldNotBeFound("flowDesc.equals=" + UPDATED_FLOW_DESC);
    }

    @Test
    @Transactional
    void getAllFlowsByFlowDescIsInShouldWork() throws Exception {
        // Initialize the database
        flowRepository.saveAndFlush(flowEntity);

        // Get all the flowList where flowDesc in DEFAULT_FLOW_DESC or UPDATED_FLOW_DESC
        defaultFlowShouldBeFound("flowDesc.in=" + DEFAULT_FLOW_DESC + "," + UPDATED_FLOW_DESC);

        // Get all the flowList where flowDesc equals to UPDATED_FLOW_DESC
        defaultFlowShouldNotBeFound("flowDesc.in=" + UPDATED_FLOW_DESC);
    }

    @Test
    @Transactional
    void getAllFlowsByFlowDescIsNullOrNotNull() throws Exception {
        // Initialize the database
        flowRepository.saveAndFlush(flowEntity);

        // Get all the flowList where flowDesc is not null
        defaultFlowShouldBeFound("flowDesc.specified=true");

        // Get all the flowList where flowDesc is null
        defaultFlowShouldNotBeFound("flowDesc.specified=false");
    }

    @Test
    @Transactional
    void getAllFlowsByFlowDescContainsSomething() throws Exception {
        // Initialize the database
        flowRepository.saveAndFlush(flowEntity);

        // Get all the flowList where flowDesc contains DEFAULT_FLOW_DESC
        defaultFlowShouldBeFound("flowDesc.contains=" + DEFAULT_FLOW_DESC);

        // Get all the flowList where flowDesc contains UPDATED_FLOW_DESC
        defaultFlowShouldNotBeFound("flowDesc.contains=" + UPDATED_FLOW_DESC);
    }

    @Test
    @Transactional
    void getAllFlowsByFlowDescNotContainsSomething() throws Exception {
        // Initialize the database
        flowRepository.saveAndFlush(flowEntity);

        // Get all the flowList where flowDesc does not contain DEFAULT_FLOW_DESC
        defaultFlowShouldNotBeFound("flowDesc.doesNotContain=" + DEFAULT_FLOW_DESC);

        // Get all the flowList where flowDesc does not contain UPDATED_FLOW_DESC
        defaultFlowShouldBeFound("flowDesc.doesNotContain=" + UPDATED_FLOW_DESC);
    }

    @Test
    @Transactional
    void getAllFlowsByFlowIsEqualToSomething() throws Exception {
        // Initialize the database
        flowRepository.saveAndFlush(flowEntity);

        // Get all the flowList where flow equals to DEFAULT_FLOW
        defaultFlowShouldBeFound("flow.equals=" + DEFAULT_FLOW);

        // Get all the flowList where flow equals to UPDATED_FLOW
        defaultFlowShouldNotBeFound("flow.equals=" + UPDATED_FLOW);
    }

    @Test
    @Transactional
    void getAllFlowsByFlowIsInShouldWork() throws Exception {
        // Initialize the database
        flowRepository.saveAndFlush(flowEntity);

        // Get all the flowList where flow in DEFAULT_FLOW or UPDATED_FLOW
        defaultFlowShouldBeFound("flow.in=" + DEFAULT_FLOW + "," + UPDATED_FLOW);

        // Get all the flowList where flow equals to UPDATED_FLOW
        defaultFlowShouldNotBeFound("flow.in=" + UPDATED_FLOW);
    }

    @Test
    @Transactional
    void getAllFlowsByFlowIsNullOrNotNull() throws Exception {
        // Initialize the database
        flowRepository.saveAndFlush(flowEntity);

        // Get all the flowList where flow is not null
        defaultFlowShouldBeFound("flow.specified=true");

        // Get all the flowList where flow is null
        defaultFlowShouldNotBeFound("flow.specified=false");
    }

    @Test
    @Transactional
    void getAllFlowsByFlowContainsSomething() throws Exception {
        // Initialize the database
        flowRepository.saveAndFlush(flowEntity);

        // Get all the flowList where flow contains DEFAULT_FLOW
        defaultFlowShouldBeFound("flow.contains=" + DEFAULT_FLOW);

        // Get all the flowList where flow contains UPDATED_FLOW
        defaultFlowShouldNotBeFound("flow.contains=" + UPDATED_FLOW);
    }

    @Test
    @Transactional
    void getAllFlowsByFlowNotContainsSomething() throws Exception {
        // Initialize the database
        flowRepository.saveAndFlush(flowEntity);

        // Get all the flowList where flow does not contain DEFAULT_FLOW
        defaultFlowShouldNotBeFound("flow.doesNotContain=" + DEFAULT_FLOW);

        // Get all the flowList where flow does not contain UPDATED_FLOW
        defaultFlowShouldBeFound("flow.doesNotContain=" + UPDATED_FLOW);
    }

    @Test
    @Transactional
    void getAllFlowsByProductIsEqualToSomething() throws Exception {
        ProductEntity product;
        if (TestUtil.findAll(em, ProductEntity.class).isEmpty()) {
            flowRepository.saveAndFlush(flowEntity);
            product = ProductResourceIT.createEntity(em);
        } else {
            product = TestUtil.findAll(em, ProductEntity.class).get(0);
        }
        em.persist(product);
        em.flush();
        flowEntity.setProduct(product);
        flowRepository.saveAndFlush(flowEntity);
        Long productId = product.getId();

        // Get all the flowList where product equals to productId
        defaultFlowShouldBeFound("productId.equals=" + productId);

        // Get all the flowList where product equals to (productId + 1)
        defaultFlowShouldNotBeFound("productId.equals=" + (productId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultFlowShouldBeFound(String filter) throws Exception {
        restFlowMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(flowEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].flowName").value(hasItem(DEFAULT_FLOW_NAME)))
            .andExpect(jsonPath("$.[*].flowDesc").value(hasItem(DEFAULT_FLOW_DESC)))
            .andExpect(jsonPath("$.[*].flow").value(hasItem(DEFAULT_FLOW)));

        // Check, that the count call also returns 1
        restFlowMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultFlowShouldNotBeFound(String filter) throws Exception {
        restFlowMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restFlowMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingFlow() throws Exception {
        // Get the flow
        restFlowMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingFlow() throws Exception {
        // Initialize the database
        flowRepository.saveAndFlush(flowEntity);

        int databaseSizeBeforeUpdate = flowRepository.findAll().size();

        // Update the flow
        FlowEntity updatedFlowEntity = flowRepository.findById(flowEntity.getId()).get();
        // Disconnect from session so that the updates on updatedFlowEntity are not directly saved in db
        em.detach(updatedFlowEntity);
        updatedFlowEntity.flowName(UPDATED_FLOW_NAME).flowDesc(UPDATED_FLOW_DESC).flow(UPDATED_FLOW);
        FlowDTO flowDTO = flowMapper.toDto(updatedFlowEntity);

        restFlowMockMvc
            .perform(
                put(ENTITY_API_URL_ID, flowDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(flowDTO))
            )
            .andExpect(status().isOk());

        // Validate the Flow in the database
        List<FlowEntity> flowList = flowRepository.findAll();
        assertThat(flowList).hasSize(databaseSizeBeforeUpdate);
        FlowEntity testFlow = flowList.get(flowList.size() - 1);
        assertThat(testFlow.getFlowName()).isEqualTo(UPDATED_FLOW_NAME);
        assertThat(testFlow.getFlowDesc()).isEqualTo(UPDATED_FLOW_DESC);
        assertThat(testFlow.getFlow()).isEqualTo(UPDATED_FLOW);
    }

    @Test
    @Transactional
    void putNonExistingFlow() throws Exception {
        int databaseSizeBeforeUpdate = flowRepository.findAll().size();
        flowEntity.setId(count.incrementAndGet());

        // Create the Flow
        FlowDTO flowDTO = flowMapper.toDto(flowEntity);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFlowMockMvc
            .perform(
                put(ENTITY_API_URL_ID, flowDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(flowDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Flow in the database
        List<FlowEntity> flowList = flowRepository.findAll();
        assertThat(flowList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchFlow() throws Exception {
        int databaseSizeBeforeUpdate = flowRepository.findAll().size();
        flowEntity.setId(count.incrementAndGet());

        // Create the Flow
        FlowDTO flowDTO = flowMapper.toDto(flowEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFlowMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(flowDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Flow in the database
        List<FlowEntity> flowList = flowRepository.findAll();
        assertThat(flowList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamFlow() throws Exception {
        int databaseSizeBeforeUpdate = flowRepository.findAll().size();
        flowEntity.setId(count.incrementAndGet());

        // Create the Flow
        FlowDTO flowDTO = flowMapper.toDto(flowEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFlowMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(flowDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Flow in the database
        List<FlowEntity> flowList = flowRepository.findAll();
        assertThat(flowList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateFlowWithPatch() throws Exception {
        // Initialize the database
        flowRepository.saveAndFlush(flowEntity);

        int databaseSizeBeforeUpdate = flowRepository.findAll().size();

        // Update the flow using partial update
        FlowEntity partialUpdatedFlowEntity = new FlowEntity();
        partialUpdatedFlowEntity.setId(flowEntity.getId());

        partialUpdatedFlowEntity.flowName(UPDATED_FLOW_NAME).flowDesc(UPDATED_FLOW_DESC);

        restFlowMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFlowEntity.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedFlowEntity))
            )
            .andExpect(status().isOk());

        // Validate the Flow in the database
        List<FlowEntity> flowList = flowRepository.findAll();
        assertThat(flowList).hasSize(databaseSizeBeforeUpdate);
        FlowEntity testFlow = flowList.get(flowList.size() - 1);
        assertThat(testFlow.getFlowName()).isEqualTo(UPDATED_FLOW_NAME);
        assertThat(testFlow.getFlowDesc()).isEqualTo(UPDATED_FLOW_DESC);
        assertThat(testFlow.getFlow()).isEqualTo(DEFAULT_FLOW);
    }

    @Test
    @Transactional
    void fullUpdateFlowWithPatch() throws Exception {
        // Initialize the database
        flowRepository.saveAndFlush(flowEntity);

        int databaseSizeBeforeUpdate = flowRepository.findAll().size();

        // Update the flow using partial update
        FlowEntity partialUpdatedFlowEntity = new FlowEntity();
        partialUpdatedFlowEntity.setId(flowEntity.getId());

        partialUpdatedFlowEntity.flowName(UPDATED_FLOW_NAME).flowDesc(UPDATED_FLOW_DESC).flow(UPDATED_FLOW);

        restFlowMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFlowEntity.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedFlowEntity))
            )
            .andExpect(status().isOk());

        // Validate the Flow in the database
        List<FlowEntity> flowList = flowRepository.findAll();
        assertThat(flowList).hasSize(databaseSizeBeforeUpdate);
        FlowEntity testFlow = flowList.get(flowList.size() - 1);
        assertThat(testFlow.getFlowName()).isEqualTo(UPDATED_FLOW_NAME);
        assertThat(testFlow.getFlowDesc()).isEqualTo(UPDATED_FLOW_DESC);
        assertThat(testFlow.getFlow()).isEqualTo(UPDATED_FLOW);
    }

    @Test
    @Transactional
    void patchNonExistingFlow() throws Exception {
        int databaseSizeBeforeUpdate = flowRepository.findAll().size();
        flowEntity.setId(count.incrementAndGet());

        // Create the Flow
        FlowDTO flowDTO = flowMapper.toDto(flowEntity);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFlowMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, flowDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(flowDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Flow in the database
        List<FlowEntity> flowList = flowRepository.findAll();
        assertThat(flowList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchFlow() throws Exception {
        int databaseSizeBeforeUpdate = flowRepository.findAll().size();
        flowEntity.setId(count.incrementAndGet());

        // Create the Flow
        FlowDTO flowDTO = flowMapper.toDto(flowEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFlowMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(flowDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Flow in the database
        List<FlowEntity> flowList = flowRepository.findAll();
        assertThat(flowList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamFlow() throws Exception {
        int databaseSizeBeforeUpdate = flowRepository.findAll().size();
        flowEntity.setId(count.incrementAndGet());

        // Create the Flow
        FlowDTO flowDTO = flowMapper.toDto(flowEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFlowMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(flowDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Flow in the database
        List<FlowEntity> flowList = flowRepository.findAll();
        assertThat(flowList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteFlow() throws Exception {
        // Initialize the database
        flowRepository.saveAndFlush(flowEntity);

        int databaseSizeBeforeDelete = flowRepository.findAll().size();

        // Delete the flow
        restFlowMockMvc
            .perform(delete(ENTITY_API_URL_ID, flowEntity.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<FlowEntity> flowList = flowRepository.findAll();
        assertThat(flowList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
