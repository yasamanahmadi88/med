/*
package com.behsa.medportal.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.behsa.medportal.IntegrationTest;
import com.behsa.medportal.domain.CustomAuditEventEntity;
import com.behsa.medportal.repository.CustomAuditEventRepository;
import com.behsa.medportal.service.criteria.CustomAuditEventCriteria;
import com.behsa.medportal.service.dto.CustomAuditEventDTO;
import com.behsa.medportal.service.mapper.CustomAuditEventMapper;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

*/
/**
 * Integration tests for the {@link CustomAuditEventResource} REST controller.
 *//*

@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CustomAuditEventResourceIT {

    private static final String DEFAULT_PRINCIPAL = "AAAAAAAAAA";
    private static final String UPDATED_PRINCIPAL = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_EVENT_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_EVENT_DATE = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_EVENT_DATE = LocalDate.ofEpochDay(-1L);

    private static final String DEFAULT_EVENT_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_EVENT_TYPE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/custom-audit-events";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private CustomAuditEventRepository customAuditEventRepository;

    @Autowired
    private CustomAuditEventMapper customAuditEventMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCustomAuditEventMockMvc;

    private CustomAuditEventEntity customAuditEventEntity;

    */
/**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     *//*

    public static CustomAuditEventEntity createEntity(EntityManager em) {
        CustomAuditEventEntity customAuditEventEntity = new CustomAuditEventEntity()
            .principal(DEFAULT_PRINCIPAL)
            .eventDate(DEFAULT_EVENT_DATE)
            .eventType(DEFAULT_EVENT_TYPE);
        return customAuditEventEntity;
    }

    */
/**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     *//*

    public static CustomAuditEventEntity createUpdatedEntity(EntityManager em) {
        CustomAuditEventEntity customAuditEventEntity = new CustomAuditEventEntity()
            .principal(UPDATED_PRINCIPAL)
            .eventDate(UPDATED_EVENT_DATE)
            .eventType(UPDATED_EVENT_TYPE);
        return customAuditEventEntity;
    }

    @BeforeEach
    public void initTest() {
        customAuditEventEntity = createEntity(em);
    }

    @Test
    @Transactional
    void createCustomAuditEvent() throws Exception {
        int databaseSizeBeforeCreate = customAuditEventRepository.findAll().size();
        // Create the CustomAuditEvent
        CustomAuditEventDTO customAuditEventDTO = customAuditEventMapper.toDto(customAuditEventEntity);
        restCustomAuditEventMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(customAuditEventDTO))
            )
            .andExpect(status().isCreated());

        // Validate the CustomAuditEvent in the database
        List<CustomAuditEventEntity> customAuditEventList = customAuditEventRepository.findAll();
        assertThat(customAuditEventList).hasSize(databaseSizeBeforeCreate + 1);
        CustomAuditEventEntity testCustomAuditEvent = customAuditEventList.get(customAuditEventList.size() - 1);
        assertThat(testCustomAuditEvent.getPrincipal()).isEqualTo(DEFAULT_PRINCIPAL);
        assertThat(testCustomAuditEvent.getEventDate()).isEqualTo(DEFAULT_EVENT_DATE);
        assertThat(testCustomAuditEvent.getEventType()).isEqualTo(DEFAULT_EVENT_TYPE);
    }

    @Test
    @Transactional
    void createCustomAuditEventWithExistingId() throws Exception {
        // Create the CustomAuditEvent with an existing ID
        customAuditEventEntity.setId(1L);
        CustomAuditEventDTO customAuditEventDTO = customAuditEventMapper.toDto(customAuditEventEntity);

        int databaseSizeBeforeCreate = customAuditEventRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCustomAuditEventMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(customAuditEventDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CustomAuditEvent in the database
        List<CustomAuditEventEntity> customAuditEventList = customAuditEventRepository.findAll();
        assertThat(customAuditEventList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkPrincipalIsRequired() throws Exception {
        int databaseSizeBeforeTest = customAuditEventRepository.findAll().size();
        // set the field null
        customAuditEventEntity.setPrincipal(null);

        // Create the CustomAuditEvent, which fails.
        CustomAuditEventDTO customAuditEventDTO = customAuditEventMapper.toDto(customAuditEventEntity);

        restCustomAuditEventMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(customAuditEventDTO))
            )
            .andExpect(status().isBadRequest());

        List<CustomAuditEventEntity> customAuditEventList = customAuditEventRepository.findAll();
        assertThat(customAuditEventList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllCustomAuditEvents() throws Exception {
        // Initialize the database
        customAuditEventRepository.saveAndFlush(customAuditEventEntity);

        // Get all the customAuditEventList
        restCustomAuditEventMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(customAuditEventEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].principal").value(hasItem(DEFAULT_PRINCIPAL)))
            .andExpect(jsonPath("$.[*].eventDate").value(hasItem(DEFAULT_EVENT_DATE.toString())))
            .andExpect(jsonPath("$.[*].eventType").value(hasItem(DEFAULT_EVENT_TYPE)));
    }

    @Test
    @Transactional
    void getCustomAuditEvent() throws Exception {
        // Initialize the database
        customAuditEventRepository.saveAndFlush(customAuditEventEntity);

        // Get the customAuditEvent
        restCustomAuditEventMockMvc
            .perform(get(ENTITY_API_URL_ID, customAuditEventEntity.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(customAuditEventEntity.getId().intValue()))
            .andExpect(jsonPath("$.principal").value(DEFAULT_PRINCIPAL))
            .andExpect(jsonPath("$.eventDate").value(DEFAULT_EVENT_DATE.toString()))
            .andExpect(jsonPath("$.eventType").value(DEFAULT_EVENT_TYPE));
    }

    @Test
    @Transactional
    void getCustomAuditEventsByIdFiltering() throws Exception {
        // Initialize the database
        customAuditEventRepository.saveAndFlush(customAuditEventEntity);

        Long id = customAuditEventEntity.getId();

        defaultCustomAuditEventShouldBeFound("id.equals=" + id);
        defaultCustomAuditEventShouldNotBeFound("id.notEquals=" + id);

        defaultCustomAuditEventShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultCustomAuditEventShouldNotBeFound("id.greaterThan=" + id);

        defaultCustomAuditEventShouldBeFound("id.lessThanOrEqual=" + id);
        defaultCustomAuditEventShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllCustomAuditEventsByPrincipalIsEqualToSomething() throws Exception {
        // Initialize the database
        customAuditEventRepository.saveAndFlush(customAuditEventEntity);

        // Get all the customAuditEventList where principal equals to DEFAULT_PRINCIPAL
        defaultCustomAuditEventShouldBeFound("principal.equals=" + DEFAULT_PRINCIPAL);

        // Get all the customAuditEventList where principal equals to UPDATED_PRINCIPAL
        defaultCustomAuditEventShouldNotBeFound("principal.equals=" + UPDATED_PRINCIPAL);
    }

    @Test
    @Transactional
    void getAllCustomAuditEventsByPrincipalIsInShouldWork() throws Exception {
        // Initialize the database
        customAuditEventRepository.saveAndFlush(customAuditEventEntity);

        // Get all the customAuditEventList where principal in DEFAULT_PRINCIPAL or UPDATED_PRINCIPAL
        defaultCustomAuditEventShouldBeFound("principal.in=" + DEFAULT_PRINCIPAL + "," + UPDATED_PRINCIPAL);

        // Get all the customAuditEventList where principal equals to UPDATED_PRINCIPAL
        defaultCustomAuditEventShouldNotBeFound("principal.in=" + UPDATED_PRINCIPAL);
    }

    @Test
    @Transactional
    void getAllCustomAuditEventsByPrincipalIsNullOrNotNull() throws Exception {
        // Initialize the database
        customAuditEventRepository.saveAndFlush(customAuditEventEntity);

        // Get all the customAuditEventList where principal is not null
        defaultCustomAuditEventShouldBeFound("principal.specified=true");

        // Get all the customAuditEventList where principal is null
        defaultCustomAuditEventShouldNotBeFound("principal.specified=false");
    }

    @Test
    @Transactional
    void getAllCustomAuditEventsByPrincipalContainsSomething() throws Exception {
        // Initialize the database
        customAuditEventRepository.saveAndFlush(customAuditEventEntity);

        // Get all the customAuditEventList where principal contains DEFAULT_PRINCIPAL
        defaultCustomAuditEventShouldBeFound("principal.contains=" + DEFAULT_PRINCIPAL);

        // Get all the customAuditEventList where principal contains UPDATED_PRINCIPAL
        defaultCustomAuditEventShouldNotBeFound("principal.contains=" + UPDATED_PRINCIPAL);
    }

    @Test
    @Transactional
    void getAllCustomAuditEventsByPrincipalNotContainsSomething() throws Exception {
        // Initialize the database
        customAuditEventRepository.saveAndFlush(customAuditEventEntity);

        // Get all the customAuditEventList where principal does not contain DEFAULT_PRINCIPAL
        defaultCustomAuditEventShouldNotBeFound("principal.doesNotContain=" + DEFAULT_PRINCIPAL);

        // Get all the customAuditEventList where principal does not contain UPDATED_PRINCIPAL
        defaultCustomAuditEventShouldBeFound("principal.doesNotContain=" + UPDATED_PRINCIPAL);
    }

    @Test
    @Transactional
    void getAllCustomAuditEventsByEventDateIsEqualToSomething() throws Exception {
        // Initialize the database
        customAuditEventRepository.saveAndFlush(customAuditEventEntity);

        // Get all the customAuditEventList where eventDate equals to DEFAULT_EVENT_DATE
        defaultCustomAuditEventShouldBeFound("eventDate.equals=" + DEFAULT_EVENT_DATE);

        // Get all the customAuditEventList where eventDate equals to UPDATED_EVENT_DATE
        defaultCustomAuditEventShouldNotBeFound("eventDate.equals=" + UPDATED_EVENT_DATE);
    }

    @Test
    @Transactional
    void getAllCustomAuditEventsByEventDateIsInShouldWork() throws Exception {
        // Initialize the database
        customAuditEventRepository.saveAndFlush(customAuditEventEntity);

        // Get all the customAuditEventList where eventDate in DEFAULT_EVENT_DATE or UPDATED_EVENT_DATE
        defaultCustomAuditEventShouldBeFound("eventDate.in=" + DEFAULT_EVENT_DATE + "," + UPDATED_EVENT_DATE);

        // Get all the customAuditEventList where eventDate equals to UPDATED_EVENT_DATE
        defaultCustomAuditEventShouldNotBeFound("eventDate.in=" + UPDATED_EVENT_DATE);
    }

    @Test
    @Transactional
    void getAllCustomAuditEventsByEventDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        customAuditEventRepository.saveAndFlush(customAuditEventEntity);

        // Get all the customAuditEventList where eventDate is not null
        defaultCustomAuditEventShouldBeFound("eventDate.specified=true");

        // Get all the customAuditEventList where eventDate is null
        defaultCustomAuditEventShouldNotBeFound("eventDate.specified=false");
    }

    @Test
    @Transactional
    void getAllCustomAuditEventsByEventDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        customAuditEventRepository.saveAndFlush(customAuditEventEntity);

        // Get all the customAuditEventList where eventDate is greater than or equal to DEFAULT_EVENT_DATE
        defaultCustomAuditEventShouldBeFound("eventDate.greaterThanOrEqual=" + DEFAULT_EVENT_DATE);

        // Get all the customAuditEventList where eventDate is greater than or equal to UPDATED_EVENT_DATE
        defaultCustomAuditEventShouldNotBeFound("eventDate.greaterThanOrEqual=" + UPDATED_EVENT_DATE);
    }

    @Test
    @Transactional
    void getAllCustomAuditEventsByEventDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        customAuditEventRepository.saveAndFlush(customAuditEventEntity);

        // Get all the customAuditEventList where eventDate is less than or equal to DEFAULT_EVENT_DATE
        defaultCustomAuditEventShouldBeFound("eventDate.lessThanOrEqual=" + DEFAULT_EVENT_DATE);

        // Get all the customAuditEventList where eventDate is less than or equal to SMALLER_EVENT_DATE
        defaultCustomAuditEventShouldNotBeFound("eventDate.lessThanOrEqual=" + SMALLER_EVENT_DATE);
    }

    @Test
    @Transactional
    void getAllCustomAuditEventsByEventDateIsLessThanSomething() throws Exception {
        // Initialize the database
        customAuditEventRepository.saveAndFlush(customAuditEventEntity);

        // Get all the customAuditEventList where eventDate is less than DEFAULT_EVENT_DATE
        defaultCustomAuditEventShouldNotBeFound("eventDate.lessThan=" + DEFAULT_EVENT_DATE);

        // Get all the customAuditEventList where eventDate is less than UPDATED_EVENT_DATE
        defaultCustomAuditEventShouldBeFound("eventDate.lessThan=" + UPDATED_EVENT_DATE);
    }

    @Test
    @Transactional
    void getAllCustomAuditEventsByEventDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        customAuditEventRepository.saveAndFlush(customAuditEventEntity);

        // Get all the customAuditEventList where eventDate is greater than DEFAULT_EVENT_DATE
        defaultCustomAuditEventShouldNotBeFound("eventDate.greaterThan=" + DEFAULT_EVENT_DATE);

        // Get all the customAuditEventList where eventDate is greater than SMALLER_EVENT_DATE
        defaultCustomAuditEventShouldBeFound("eventDate.greaterThan=" + SMALLER_EVENT_DATE);
    }

    @Test
    @Transactional
    void getAllCustomAuditEventsByEventTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        customAuditEventRepository.saveAndFlush(customAuditEventEntity);

        // Get all the customAuditEventList where eventType equals to DEFAULT_EVENT_TYPE
        defaultCustomAuditEventShouldBeFound("eventType.equals=" + DEFAULT_EVENT_TYPE);

        // Get all the customAuditEventList where eventType equals to UPDATED_EVENT_TYPE
        defaultCustomAuditEventShouldNotBeFound("eventType.equals=" + UPDATED_EVENT_TYPE);
    }

    @Test
    @Transactional
    void getAllCustomAuditEventsByEventTypeIsInShouldWork() throws Exception {
        // Initialize the database
        customAuditEventRepository.saveAndFlush(customAuditEventEntity);

        // Get all the customAuditEventList where eventType in DEFAULT_EVENT_TYPE or UPDATED_EVENT_TYPE
        defaultCustomAuditEventShouldBeFound("eventType.in=" + DEFAULT_EVENT_TYPE + "," + UPDATED_EVENT_TYPE);

        // Get all the customAuditEventList where eventType equals to UPDATED_EVENT_TYPE
        defaultCustomAuditEventShouldNotBeFound("eventType.in=" + UPDATED_EVENT_TYPE);
    }

    @Test
    @Transactional
    void getAllCustomAuditEventsByEventTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        customAuditEventRepository.saveAndFlush(customAuditEventEntity);

        // Get all the customAuditEventList where eventType is not null
        defaultCustomAuditEventShouldBeFound("eventType.specified=true");

        // Get all the customAuditEventList where eventType is null
        defaultCustomAuditEventShouldNotBeFound("eventType.specified=false");
    }

    @Test
    @Transactional
    void getAllCustomAuditEventsByEventTypeContainsSomething() throws Exception {
        // Initialize the database
        customAuditEventRepository.saveAndFlush(customAuditEventEntity);

        // Get all the customAuditEventList where eventType contains DEFAULT_EVENT_TYPE
        defaultCustomAuditEventShouldBeFound("eventType.contains=" + DEFAULT_EVENT_TYPE);

        // Get all the customAuditEventList where eventType contains UPDATED_EVENT_TYPE
        defaultCustomAuditEventShouldNotBeFound("eventType.contains=" + UPDATED_EVENT_TYPE);
    }

    @Test
    @Transactional
    void getAllCustomAuditEventsByEventTypeNotContainsSomething() throws Exception {
        // Initialize the database
        customAuditEventRepository.saveAndFlush(customAuditEventEntity);

        // Get all the customAuditEventList where eventType does not contain DEFAULT_EVENT_TYPE
        defaultCustomAuditEventShouldNotBeFound("eventType.doesNotContain=" + DEFAULT_EVENT_TYPE);

        // Get all the customAuditEventList where eventType does not contain UPDATED_EVENT_TYPE
        defaultCustomAuditEventShouldBeFound("eventType.doesNotContain=" + UPDATED_EVENT_TYPE);
    }

    */
/**
     * Executes the search, and checks that the default entity is returned.
     *//*

    private void defaultCustomAuditEventShouldBeFound(String filter) throws Exception {
        restCustomAuditEventMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(customAuditEventEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].principal").value(hasItem(DEFAULT_PRINCIPAL)))
            .andExpect(jsonPath("$.[*].eventDate").value(hasItem(DEFAULT_EVENT_DATE.toString())))
            .andExpect(jsonPath("$.[*].eventType").value(hasItem(DEFAULT_EVENT_TYPE)));

        // Check, that the count call also returns 1
        restCustomAuditEventMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    */
/**
     * Executes the search, and checks that the default entity is not returned.
     *//*

    private void defaultCustomAuditEventShouldNotBeFound(String filter) throws Exception {
        restCustomAuditEventMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCustomAuditEventMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingCustomAuditEvent() throws Exception {
        // Get the customAuditEvent
        restCustomAuditEventMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingCustomAuditEvent() throws Exception {
        // Initialize the database
        customAuditEventRepository.saveAndFlush(customAuditEventEntity);

        int databaseSizeBeforeUpdate = customAuditEventRepository.findAll().size();

        // Update the customAuditEvent
        CustomAuditEventEntity updatedCustomAuditEventEntity = customAuditEventRepository.findById(customAuditEventEntity.getId()).get();
        // Disconnect from session so that the updates on updatedCustomAuditEventEntity are not directly saved in db
        em.detach(updatedCustomAuditEventEntity);
        updatedCustomAuditEventEntity.principal(UPDATED_PRINCIPAL).eventDate(UPDATED_EVENT_DATE).eventType(UPDATED_EVENT_TYPE);
        CustomAuditEventDTO customAuditEventDTO = customAuditEventMapper.toDto(updatedCustomAuditEventEntity);

        restCustomAuditEventMockMvc
            .perform(
                put(ENTITY_API_URL_ID, customAuditEventDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(customAuditEventDTO))
            )
            .andExpect(status().isOk());

        // Validate the CustomAuditEvent in the database
        List<CustomAuditEventEntity> customAuditEventList = customAuditEventRepository.findAll();
        assertThat(customAuditEventList).hasSize(databaseSizeBeforeUpdate);
        CustomAuditEventEntity testCustomAuditEvent = customAuditEventList.get(customAuditEventList.size() - 1);
        assertThat(testCustomAuditEvent.getPrincipal()).isEqualTo(UPDATED_PRINCIPAL);
        assertThat(testCustomAuditEvent.getEventDate()).isEqualTo(UPDATED_EVENT_DATE);
        assertThat(testCustomAuditEvent.getEventType()).isEqualTo(UPDATED_EVENT_TYPE);
    }

    @Test
    @Transactional
    void putNonExistingCustomAuditEvent() throws Exception {
        int databaseSizeBeforeUpdate = customAuditEventRepository.findAll().size();
        customAuditEventEntity.setId(count.incrementAndGet());

        // Create the CustomAuditEvent
        CustomAuditEventDTO customAuditEventDTO = customAuditEventMapper.toDto(customAuditEventEntity);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCustomAuditEventMockMvc
            .perform(
                put(ENTITY_API_URL_ID, customAuditEventDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(customAuditEventDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CustomAuditEvent in the database
        List<CustomAuditEventEntity> customAuditEventList = customAuditEventRepository.findAll();
        assertThat(customAuditEventList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCustomAuditEvent() throws Exception {
        int databaseSizeBeforeUpdate = customAuditEventRepository.findAll().size();
        customAuditEventEntity.setId(count.incrementAndGet());

        // Create the CustomAuditEvent
        CustomAuditEventDTO customAuditEventDTO = customAuditEventMapper.toDto(customAuditEventEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCustomAuditEventMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(customAuditEventDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CustomAuditEvent in the database
        List<CustomAuditEventEntity> customAuditEventList = customAuditEventRepository.findAll();
        assertThat(customAuditEventList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCustomAuditEvent() throws Exception {
        int databaseSizeBeforeUpdate = customAuditEventRepository.findAll().size();
        customAuditEventEntity.setId(count.incrementAndGet());

        // Create the CustomAuditEvent
        CustomAuditEventDTO customAuditEventDTO = customAuditEventMapper.toDto(customAuditEventEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCustomAuditEventMockMvc
            .perform(
                put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(customAuditEventDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the CustomAuditEvent in the database
        List<CustomAuditEventEntity> customAuditEventList = customAuditEventRepository.findAll();
        assertThat(customAuditEventList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCustomAuditEventWithPatch() throws Exception {
        // Initialize the database
        customAuditEventRepository.saveAndFlush(customAuditEventEntity);

        int databaseSizeBeforeUpdate = customAuditEventRepository.findAll().size();

        // Update the customAuditEvent using partial update
        CustomAuditEventEntity partialUpdatedCustomAuditEventEntity = new CustomAuditEventEntity();
        partialUpdatedCustomAuditEventEntity.setId(customAuditEventEntity.getId());

        partialUpdatedCustomAuditEventEntity.eventType(UPDATED_EVENT_TYPE);

        restCustomAuditEventMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCustomAuditEventEntity.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCustomAuditEventEntity))
            )
            .andExpect(status().isOk());

        // Validate the CustomAuditEvent in the database
        List<CustomAuditEventEntity> customAuditEventList = customAuditEventRepository.findAll();
        assertThat(customAuditEventList).hasSize(databaseSizeBeforeUpdate);
        CustomAuditEventEntity testCustomAuditEvent = customAuditEventList.get(customAuditEventList.size() - 1);
        assertThat(testCustomAuditEvent.getPrincipal()).isEqualTo(DEFAULT_PRINCIPAL);
        assertThat(testCustomAuditEvent.getEventDate()).isEqualTo(DEFAULT_EVENT_DATE);
        assertThat(testCustomAuditEvent.getEventType()).isEqualTo(UPDATED_EVENT_TYPE);
    }

    @Test
    @Transactional
    void fullUpdateCustomAuditEventWithPatch() throws Exception {
        // Initialize the database
        customAuditEventRepository.saveAndFlush(customAuditEventEntity);

        int databaseSizeBeforeUpdate = customAuditEventRepository.findAll().size();

        // Update the customAuditEvent using partial update
        CustomAuditEventEntity partialUpdatedCustomAuditEventEntity = new CustomAuditEventEntity();
        partialUpdatedCustomAuditEventEntity.setId(customAuditEventEntity.getId());

        partialUpdatedCustomAuditEventEntity.principal(UPDATED_PRINCIPAL).eventDate(UPDATED_EVENT_DATE).eventType(UPDATED_EVENT_TYPE);

        restCustomAuditEventMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCustomAuditEventEntity.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCustomAuditEventEntity))
            )
            .andExpect(status().isOk());

        // Validate the CustomAuditEvent in the database
        List<CustomAuditEventEntity> customAuditEventList = customAuditEventRepository.findAll();
        assertThat(customAuditEventList).hasSize(databaseSizeBeforeUpdate);
        CustomAuditEventEntity testCustomAuditEvent = customAuditEventList.get(customAuditEventList.size() - 1);
        assertThat(testCustomAuditEvent.getPrincipal()).isEqualTo(UPDATED_PRINCIPAL);
        assertThat(testCustomAuditEvent.getEventDate()).isEqualTo(UPDATED_EVENT_DATE);
        assertThat(testCustomAuditEvent.getEventType()).isEqualTo(UPDATED_EVENT_TYPE);
    }

    @Test
    @Transactional
    void patchNonExistingCustomAuditEvent() throws Exception {
        int databaseSizeBeforeUpdate = customAuditEventRepository.findAll().size();
        customAuditEventEntity.setId(count.incrementAndGet());

        // Create the CustomAuditEvent
        CustomAuditEventDTO customAuditEventDTO = customAuditEventMapper.toDto(customAuditEventEntity);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCustomAuditEventMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, customAuditEventDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(customAuditEventDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CustomAuditEvent in the database
        List<CustomAuditEventEntity> customAuditEventList = customAuditEventRepository.findAll();
        assertThat(customAuditEventList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCustomAuditEvent() throws Exception {
        int databaseSizeBeforeUpdate = customAuditEventRepository.findAll().size();
        customAuditEventEntity.setId(count.incrementAndGet());

        // Create the CustomAuditEvent
        CustomAuditEventDTO customAuditEventDTO = customAuditEventMapper.toDto(customAuditEventEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCustomAuditEventMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(customAuditEventDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CustomAuditEvent in the database
        List<CustomAuditEventEntity> customAuditEventList = customAuditEventRepository.findAll();
        assertThat(customAuditEventList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCustomAuditEvent() throws Exception {
        int databaseSizeBeforeUpdate = customAuditEventRepository.findAll().size();
        customAuditEventEntity.setId(count.incrementAndGet());

        // Create the CustomAuditEvent
        CustomAuditEventDTO customAuditEventDTO = customAuditEventMapper.toDto(customAuditEventEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCustomAuditEventMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(customAuditEventDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the CustomAuditEvent in the database
        List<CustomAuditEventEntity> customAuditEventList = customAuditEventRepository.findAll();
        assertThat(customAuditEventList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCustomAuditEvent() throws Exception {
        // Initialize the database
        customAuditEventRepository.saveAndFlush(customAuditEventEntity);

        int databaseSizeBeforeDelete = customAuditEventRepository.findAll().size();

        // Delete the customAuditEvent
        restCustomAuditEventMockMvc
            .perform(delete(ENTITY_API_URL_ID, customAuditEventEntity.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<CustomAuditEventEntity> customAuditEventList = customAuditEventRepository.findAll();
        assertThat(customAuditEventList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
*/
