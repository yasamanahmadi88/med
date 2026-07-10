/*
package com.behsa.medportal.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.behsa.medportal.IntegrationTest;
import com.behsa.medportal.domain.MedAuthorityEntity;
import com.behsa.medportal.domain.ResourceAuthorityEntity;
import com.behsa.medportal.repository.MedAuthorityRepository;
import com.behsa.medportal.service.criteria.MedAuthorityCriteria;
import com.behsa.medportal.service.dto.MedAuthorityDTO;
import com.behsa.medportal.service.mapper.MedAuthorityMapper;
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
 * Integration tests for the {@link MedAuthorityResource} REST controller.
 *//*

@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class MedAuthorityResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DISPLAY_NAME = "AAAAAAAAAA";
    private static final String UPDATED_DISPLAY_NAME = "BBBBBBBBBB";

    private static final Long DEFAULT_PARENT_ID = 1L;
    private static final Long UPDATED_PARENT_ID = 2L;
    private static final Long SMALLER_PARENT_ID = 1L - 1L;

    private static final String ENTITY_API_URL = "/api/med-authorities";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private MedAuthorityRepository medAuthorityRepository;

    @Autowired
    private MedAuthorityMapper medAuthorityMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMedAuthorityMockMvc;

    private MedAuthorityEntity medAuthorityEntity;

    */
/**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     *//*

    public static MedAuthorityEntity createEntity(EntityManager em) {
        MedAuthorityEntity medAuthorityEntity = new MedAuthorityEntity()
            .name(DEFAULT_NAME)
            .displayName(DEFAULT_DISPLAY_NAME)
            .parentId(DEFAULT_PARENT_ID);
        return medAuthorityEntity;
    }

    */
/**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     *//*

    public static MedAuthorityEntity createUpdatedEntity(EntityManager em) {
        MedAuthorityEntity medAuthorityEntity = new MedAuthorityEntity()
            .name(UPDATED_NAME)
            .displayName(UPDATED_DISPLAY_NAME)
            .parentId(UPDATED_PARENT_ID);
        return medAuthorityEntity;
    }

    @BeforeEach
    public void initTest() {
        medAuthorityEntity = createEntity(em);
    }

    @Test
    @Transactional
    void createMedAuthority() throws Exception {
        int databaseSizeBeforeCreate = medAuthorityRepository.findAll().size();
        // Create the MedAuthority
        MedAuthorityDTO medAuthorityDTO = medAuthorityMapper.toDto(medAuthorityEntity);
        restMedAuthorityMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(medAuthorityDTO))
            )
            .andExpect(status().isCreated());

        // Validate the MedAuthority in the database
        List<MedAuthorityEntity> medAuthorityList = medAuthorityRepository.findAll();
        assertThat(medAuthorityList).hasSize(databaseSizeBeforeCreate + 1);
        MedAuthorityEntity testMedAuthority = medAuthorityList.get(medAuthorityList.size() - 1);
        assertThat(testMedAuthority.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testMedAuthority.getDisplayName()).isEqualTo(DEFAULT_DISPLAY_NAME);
        assertThat(testMedAuthority.getParentId()).isEqualTo(DEFAULT_PARENT_ID);
    }

    @Test
    @Transactional
    void createMedAuthorityWithExistingId() throws Exception {
        // Create the MedAuthority with an existing ID
        medAuthorityEntity.setId(1L);
        MedAuthorityDTO medAuthorityDTO = medAuthorityMapper.toDto(medAuthorityEntity);

        int databaseSizeBeforeCreate = medAuthorityRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restMedAuthorityMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(medAuthorityDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MedAuthority in the database
        List<MedAuthorityEntity> medAuthorityList = medAuthorityRepository.findAll();
        assertThat(medAuthorityList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = medAuthorityRepository.findAll().size();
        // set the field null
        medAuthorityEntity.setName(null);

        // Create the MedAuthority, which fails.
        MedAuthorityDTO medAuthorityDTO = medAuthorityMapper.toDto(medAuthorityEntity);

        restMedAuthorityMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(medAuthorityDTO))
            )
            .andExpect(status().isBadRequest());

        List<MedAuthorityEntity> medAuthorityList = medAuthorityRepository.findAll();
        assertThat(medAuthorityList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllMedAuthorities() throws Exception {
        // Initialize the database
        medAuthorityRepository.saveAndFlush(medAuthorityEntity);

        // Get all the medAuthorityList
        restMedAuthorityMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(medAuthorityEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].displayName").value(hasItem(DEFAULT_DISPLAY_NAME)))
            .andExpect(jsonPath("$.[*].parentId").value(hasItem(DEFAULT_PARENT_ID.intValue())));
    }

    @Test
    @Transactional
    void getMedAuthority() throws Exception {
        // Initialize the database
        medAuthorityRepository.saveAndFlush(medAuthorityEntity);

        // Get the medAuthority
        restMedAuthorityMockMvc
            .perform(get(ENTITY_API_URL_ID, medAuthorityEntity.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(medAuthorityEntity.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.displayName").value(DEFAULT_DISPLAY_NAME))
            .andExpect(jsonPath("$.parentId").value(DEFAULT_PARENT_ID.intValue()));
    }

    @Test
    @Transactional
    void getMedAuthoritiesByIdFiltering() throws Exception {
        // Initialize the database
        medAuthorityRepository.saveAndFlush(medAuthorityEntity);

        Long id = medAuthorityEntity.getId();

        defaultMedAuthorityShouldBeFound("id.equals=" + id);
        defaultMedAuthorityShouldNotBeFound("id.notEquals=" + id);

        defaultMedAuthorityShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultMedAuthorityShouldNotBeFound("id.greaterThan=" + id);

        defaultMedAuthorityShouldBeFound("id.lessThanOrEqual=" + id);
        defaultMedAuthorityShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllMedAuthoritiesByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        medAuthorityRepository.saveAndFlush(medAuthorityEntity);

        // Get all the medAuthorityList where name equals to DEFAULT_NAME
        defaultMedAuthorityShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the medAuthorityList where name equals to UPDATED_NAME
        defaultMedAuthorityShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllMedAuthoritiesByNameIsInShouldWork() throws Exception {
        // Initialize the database
        medAuthorityRepository.saveAndFlush(medAuthorityEntity);

        // Get all the medAuthorityList where name in DEFAULT_NAME or UPDATED_NAME
        defaultMedAuthorityShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the medAuthorityList where name equals to UPDATED_NAME
        defaultMedAuthorityShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllMedAuthoritiesByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        medAuthorityRepository.saveAndFlush(medAuthorityEntity);

        // Get all the medAuthorityList where name is not null
        defaultMedAuthorityShouldBeFound("name.specified=true");

        // Get all the medAuthorityList where name is null
        defaultMedAuthorityShouldNotBeFound("name.specified=false");
    }

    @Test
    @Transactional
    void getAllMedAuthoritiesByNameContainsSomething() throws Exception {
        // Initialize the database
        medAuthorityRepository.saveAndFlush(medAuthorityEntity);

        // Get all the medAuthorityList where name contains DEFAULT_NAME
        defaultMedAuthorityShouldBeFound("name.contains=" + DEFAULT_NAME);

        // Get all the medAuthorityList where name contains UPDATED_NAME
        defaultMedAuthorityShouldNotBeFound("name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllMedAuthoritiesByNameNotContainsSomething() throws Exception {
        // Initialize the database
        medAuthorityRepository.saveAndFlush(medAuthorityEntity);

        // Get all the medAuthorityList where name does not contain DEFAULT_NAME
        defaultMedAuthorityShouldNotBeFound("name.doesNotContain=" + DEFAULT_NAME);

        // Get all the medAuthorityList where name does not contain UPDATED_NAME
        defaultMedAuthorityShouldBeFound("name.doesNotContain=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllMedAuthoritiesByDisplayNameIsEqualToSomething() throws Exception {
        // Initialize the database
        medAuthorityRepository.saveAndFlush(medAuthorityEntity);

        // Get all the medAuthorityList where displayName equals to DEFAULT_DISPLAY_NAME
        defaultMedAuthorityShouldBeFound("displayName.equals=" + DEFAULT_DISPLAY_NAME);

        // Get all the medAuthorityList where displayName equals to UPDATED_DISPLAY_NAME
        defaultMedAuthorityShouldNotBeFound("displayName.equals=" + UPDATED_DISPLAY_NAME);
    }

    @Test
    @Transactional
    void getAllMedAuthoritiesByDisplayNameIsInShouldWork() throws Exception {
        // Initialize the database
        medAuthorityRepository.saveAndFlush(medAuthorityEntity);

        // Get all the medAuthorityList where displayName in DEFAULT_DISPLAY_NAME or UPDATED_DISPLAY_NAME
        defaultMedAuthorityShouldBeFound("displayName.in=" + DEFAULT_DISPLAY_NAME + "," + UPDATED_DISPLAY_NAME);

        // Get all the medAuthorityList where displayName equals to UPDATED_DISPLAY_NAME
        defaultMedAuthorityShouldNotBeFound("displayName.in=" + UPDATED_DISPLAY_NAME);
    }

    @Test
    @Transactional
    void getAllMedAuthoritiesByDisplayNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        medAuthorityRepository.saveAndFlush(medAuthorityEntity);

        // Get all the medAuthorityList where displayName is not null
        defaultMedAuthorityShouldBeFound("displayName.specified=true");

        // Get all the medAuthorityList where displayName is null
        defaultMedAuthorityShouldNotBeFound("displayName.specified=false");
    }

    @Test
    @Transactional
    void getAllMedAuthoritiesByDisplayNameContainsSomething() throws Exception {
        // Initialize the database
        medAuthorityRepository.saveAndFlush(medAuthorityEntity);

        // Get all the medAuthorityList where displayName contains DEFAULT_DISPLAY_NAME
        defaultMedAuthorityShouldBeFound("displayName.contains=" + DEFAULT_DISPLAY_NAME);

        // Get all the medAuthorityList where displayName contains UPDATED_DISPLAY_NAME
        defaultMedAuthorityShouldNotBeFound("displayName.contains=" + UPDATED_DISPLAY_NAME);
    }

    @Test
    @Transactional
    void getAllMedAuthoritiesByDisplayNameNotContainsSomething() throws Exception {
        // Initialize the database
        medAuthorityRepository.saveAndFlush(medAuthorityEntity);

        // Get all the medAuthorityList where displayName does not contain DEFAULT_DISPLAY_NAME
        defaultMedAuthorityShouldNotBeFound("displayName.doesNotContain=" + DEFAULT_DISPLAY_NAME);

        // Get all the medAuthorityList where displayName does not contain UPDATED_DISPLAY_NAME
        defaultMedAuthorityShouldBeFound("displayName.doesNotContain=" + UPDATED_DISPLAY_NAME);
    }

    @Test
    @Transactional
    void getAllMedAuthoritiesByParentIdIsEqualToSomething() throws Exception {
        // Initialize the database
        medAuthorityRepository.saveAndFlush(medAuthorityEntity);

        // Get all the medAuthorityList where parentId equals to DEFAULT_PARENT_ID
        defaultMedAuthorityShouldBeFound("parentId.equals=" + DEFAULT_PARENT_ID);

        // Get all the medAuthorityList where parentId equals to UPDATED_PARENT_ID
        defaultMedAuthorityShouldNotBeFound("parentId.equals=" + UPDATED_PARENT_ID);
    }

    @Test
    @Transactional
    void getAllMedAuthoritiesByParentIdIsInShouldWork() throws Exception {
        // Initialize the database
        medAuthorityRepository.saveAndFlush(medAuthorityEntity);

        // Get all the medAuthorityList where parentId in DEFAULT_PARENT_ID or UPDATED_PARENT_ID
        defaultMedAuthorityShouldBeFound("parentId.in=" + DEFAULT_PARENT_ID + "," + UPDATED_PARENT_ID);

        // Get all the medAuthorityList where parentId equals to UPDATED_PARENT_ID
        defaultMedAuthorityShouldNotBeFound("parentId.in=" + UPDATED_PARENT_ID);
    }

    @Test
    @Transactional
    void getAllMedAuthoritiesByParentIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        medAuthorityRepository.saveAndFlush(medAuthorityEntity);

        // Get all the medAuthorityList where parentId is not null
        defaultMedAuthorityShouldBeFound("parentId.specified=true");

        // Get all the medAuthorityList where parentId is null
        defaultMedAuthorityShouldNotBeFound("parentId.specified=false");
    }

    @Test
    @Transactional
    void getAllMedAuthoritiesByParentIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        medAuthorityRepository.saveAndFlush(medAuthorityEntity);

        // Get all the medAuthorityList where parentId is greater than or equal to DEFAULT_PARENT_ID
        defaultMedAuthorityShouldBeFound("parentId.greaterThanOrEqual=" + DEFAULT_PARENT_ID);

        // Get all the medAuthorityList where parentId is greater than or equal to UPDATED_PARENT_ID
        defaultMedAuthorityShouldNotBeFound("parentId.greaterThanOrEqual=" + UPDATED_PARENT_ID);
    }

    @Test
    @Transactional
    void getAllMedAuthoritiesByParentIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        medAuthorityRepository.saveAndFlush(medAuthorityEntity);

        // Get all the medAuthorityList where parentId is less than or equal to DEFAULT_PARENT_ID
        defaultMedAuthorityShouldBeFound("parentId.lessThanOrEqual=" + DEFAULT_PARENT_ID);

        // Get all the medAuthorityList where parentId is less than or equal to SMALLER_PARENT_ID
        defaultMedAuthorityShouldNotBeFound("parentId.lessThanOrEqual=" + SMALLER_PARENT_ID);
    }

    @Test
    @Transactional
    void getAllMedAuthoritiesByParentIdIsLessThanSomething() throws Exception {
        // Initialize the database
        medAuthorityRepository.saveAndFlush(medAuthorityEntity);

        // Get all the medAuthorityList where parentId is less than DEFAULT_PARENT_ID
        defaultMedAuthorityShouldNotBeFound("parentId.lessThan=" + DEFAULT_PARENT_ID);

        // Get all the medAuthorityList where parentId is less than UPDATED_PARENT_ID
        defaultMedAuthorityShouldBeFound("parentId.lessThan=" + UPDATED_PARENT_ID);
    }

    @Test
    @Transactional
    void getAllMedAuthoritiesByParentIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        medAuthorityRepository.saveAndFlush(medAuthorityEntity);

        // Get all the medAuthorityList where parentId is greater than DEFAULT_PARENT_ID
        defaultMedAuthorityShouldNotBeFound("parentId.greaterThan=" + DEFAULT_PARENT_ID);

        // Get all the medAuthorityList where parentId is greater than SMALLER_PARENT_ID
        defaultMedAuthorityShouldBeFound("parentId.greaterThan=" + SMALLER_PARENT_ID);
    }

    @Test
    @Transactional
    void getAllMedAuthoritiesByResourceAuthoritiesIsEqualToSomething() throws Exception {
        ResourceAuthorityEntity resourceAuthorities;
        if (TestUtil.findAll(em, ResourceAuthorityEntity.class).isEmpty()) {
            medAuthorityRepository.saveAndFlush(medAuthorityEntity);
            resourceAuthorities = ResourceAuthorityResourceIT.createEntity(em);
        } else {
            resourceAuthorities = TestUtil.findAll(em, ResourceAuthorityEntity.class).get(0);
        }
        em.persist(resourceAuthorities);
        em.flush();
        medAuthorityEntity.addResourceAuthorities(resourceAuthorities);
        medAuthorityRepository.saveAndFlush(medAuthorityEntity);
        Long resourceAuthoritiesId = resourceAuthorities.getId();

        // Get all the medAuthorityList where resourceAuthorities equals to resourceAuthoritiesId
        defaultMedAuthorityShouldBeFound("resourceAuthoritiesId.equals=" + resourceAuthoritiesId);

        // Get all the medAuthorityList where resourceAuthorities equals to (resourceAuthoritiesId + 1)
        defaultMedAuthorityShouldNotBeFound("resourceAuthoritiesId.equals=" + (resourceAuthoritiesId + 1));
    }

    */
/**
     * Executes the search, and checks that the default entity is returned.
     *//*

    private void defaultMedAuthorityShouldBeFound(String filter) throws Exception {
        restMedAuthorityMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(medAuthorityEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].displayName").value(hasItem(DEFAULT_DISPLAY_NAME)))
            .andExpect(jsonPath("$.[*].parentId").value(hasItem(DEFAULT_PARENT_ID.intValue())));

        // Check, that the count call also returns 1
        restMedAuthorityMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    */
/**
     * Executes the search, and checks that the default entity is not returned.
     *//*

    private void defaultMedAuthorityShouldNotBeFound(String filter) throws Exception {
        restMedAuthorityMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restMedAuthorityMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingMedAuthority() throws Exception {
        // Get the medAuthority
        restMedAuthorityMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingMedAuthority() throws Exception {
        // Initialize the database
        medAuthorityRepository.saveAndFlush(medAuthorityEntity);

        int databaseSizeBeforeUpdate = medAuthorityRepository.findAll().size();

        // Update the medAuthority
        MedAuthorityEntity updatedMedAuthorityEntity = medAuthorityRepository.findById(medAuthorityEntity.getId()).get();
        // Disconnect from session so that the updates on updatedMedAuthorityEntity are not directly saved in db
        em.detach(updatedMedAuthorityEntity);
        updatedMedAuthorityEntity.name(UPDATED_NAME).displayName(UPDATED_DISPLAY_NAME).parentId(UPDATED_PARENT_ID);
        MedAuthorityDTO medAuthorityDTO = medAuthorityMapper.toDto(updatedMedAuthorityEntity);

        restMedAuthorityMockMvc
            .perform(
                put(ENTITY_API_URL_ID, medAuthorityDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(medAuthorityDTO))
            )
            .andExpect(status().isOk());

        // Validate the MedAuthority in the database
        List<MedAuthorityEntity> medAuthorityList = medAuthorityRepository.findAll();
        assertThat(medAuthorityList).hasSize(databaseSizeBeforeUpdate);
        MedAuthorityEntity testMedAuthority = medAuthorityList.get(medAuthorityList.size() - 1);
        assertThat(testMedAuthority.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testMedAuthority.getDisplayName()).isEqualTo(UPDATED_DISPLAY_NAME);
        assertThat(testMedAuthority.getParentId()).isEqualTo(UPDATED_PARENT_ID);
    }

    @Test
    @Transactional
    void putNonExistingMedAuthority() throws Exception {
        int databaseSizeBeforeUpdate = medAuthorityRepository.findAll().size();
        medAuthorityEntity.setId(count.incrementAndGet());

        // Create the MedAuthority
        MedAuthorityDTO medAuthorityDTO = medAuthorityMapper.toDto(medAuthorityEntity);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMedAuthorityMockMvc
            .perform(
                put(ENTITY_API_URL_ID, medAuthorityDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(medAuthorityDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MedAuthority in the database
        List<MedAuthorityEntity> medAuthorityList = medAuthorityRepository.findAll();
        assertThat(medAuthorityList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchMedAuthority() throws Exception {
        int databaseSizeBeforeUpdate = medAuthorityRepository.findAll().size();
        medAuthorityEntity.setId(count.incrementAndGet());

        // Create the MedAuthority
        MedAuthorityDTO medAuthorityDTO = medAuthorityMapper.toDto(medAuthorityEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMedAuthorityMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(medAuthorityDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MedAuthority in the database
        List<MedAuthorityEntity> medAuthorityList = medAuthorityRepository.findAll();
        assertThat(medAuthorityList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamMedAuthority() throws Exception {
        int databaseSizeBeforeUpdate = medAuthorityRepository.findAll().size();
        medAuthorityEntity.setId(count.incrementAndGet());

        // Create the MedAuthority
        MedAuthorityDTO medAuthorityDTO = medAuthorityMapper.toDto(medAuthorityEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMedAuthorityMockMvc
            .perform(
                put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(medAuthorityDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the MedAuthority in the database
        List<MedAuthorityEntity> medAuthorityList = medAuthorityRepository.findAll();
        assertThat(medAuthorityList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateMedAuthorityWithPatch() throws Exception {
        // Initialize the database
        medAuthorityRepository.saveAndFlush(medAuthorityEntity);

        int databaseSizeBeforeUpdate = medAuthorityRepository.findAll().size();

        // Update the medAuthority using partial update
        MedAuthorityEntity partialUpdatedMedAuthorityEntity = new MedAuthorityEntity();
        partialUpdatedMedAuthorityEntity.setId(medAuthorityEntity.getId());

        partialUpdatedMedAuthorityEntity.displayName(UPDATED_DISPLAY_NAME).parentId(UPDATED_PARENT_ID);

        restMedAuthorityMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMedAuthorityEntity.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedMedAuthorityEntity))
            )
            .andExpect(status().isOk());

        // Validate the MedAuthority in the database
        List<MedAuthorityEntity> medAuthorityList = medAuthorityRepository.findAll();
        assertThat(medAuthorityList).hasSize(databaseSizeBeforeUpdate);
        MedAuthorityEntity testMedAuthority = medAuthorityList.get(medAuthorityList.size() - 1);
        assertThat(testMedAuthority.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testMedAuthority.getDisplayName()).isEqualTo(UPDATED_DISPLAY_NAME);
        assertThat(testMedAuthority.getParentId()).isEqualTo(UPDATED_PARENT_ID);
    }

    @Test
    @Transactional
    void fullUpdateMedAuthorityWithPatch() throws Exception {
        // Initialize the database
        medAuthorityRepository.saveAndFlush(medAuthorityEntity);

        int databaseSizeBeforeUpdate = medAuthorityRepository.findAll().size();

        // Update the medAuthority using partial update
        MedAuthorityEntity partialUpdatedMedAuthorityEntity = new MedAuthorityEntity();
        partialUpdatedMedAuthorityEntity.setId(medAuthorityEntity.getId());

        partialUpdatedMedAuthorityEntity.name(UPDATED_NAME).displayName(UPDATED_DISPLAY_NAME).parentId(UPDATED_PARENT_ID);

        restMedAuthorityMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMedAuthorityEntity.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedMedAuthorityEntity))
            )
            .andExpect(status().isOk());

        // Validate the MedAuthority in the database
        List<MedAuthorityEntity> medAuthorityList = medAuthorityRepository.findAll();
        assertThat(medAuthorityList).hasSize(databaseSizeBeforeUpdate);
        MedAuthorityEntity testMedAuthority = medAuthorityList.get(medAuthorityList.size() - 1);
        assertThat(testMedAuthority.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testMedAuthority.getDisplayName()).isEqualTo(UPDATED_DISPLAY_NAME);
        assertThat(testMedAuthority.getParentId()).isEqualTo(UPDATED_PARENT_ID);
    }

    @Test
    @Transactional
    void patchNonExistingMedAuthority() throws Exception {
        int databaseSizeBeforeUpdate = medAuthorityRepository.findAll().size();
        medAuthorityEntity.setId(count.incrementAndGet());

        // Create the MedAuthority
        MedAuthorityDTO medAuthorityDTO = medAuthorityMapper.toDto(medAuthorityEntity);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMedAuthorityMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, medAuthorityDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(medAuthorityDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MedAuthority in the database
        List<MedAuthorityEntity> medAuthorityList = medAuthorityRepository.findAll();
        assertThat(medAuthorityList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchMedAuthority() throws Exception {
        int databaseSizeBeforeUpdate = medAuthorityRepository.findAll().size();
        medAuthorityEntity.setId(count.incrementAndGet());

        // Create the MedAuthority
        MedAuthorityDTO medAuthorityDTO = medAuthorityMapper.toDto(medAuthorityEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMedAuthorityMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(medAuthorityDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MedAuthority in the database
        List<MedAuthorityEntity> medAuthorityList = medAuthorityRepository.findAll();
        assertThat(medAuthorityList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamMedAuthority() throws Exception {
        int databaseSizeBeforeUpdate = medAuthorityRepository.findAll().size();
        medAuthorityEntity.setId(count.incrementAndGet());

        // Create the MedAuthority
        MedAuthorityDTO medAuthorityDTO = medAuthorityMapper.toDto(medAuthorityEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMedAuthorityMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(medAuthorityDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the MedAuthority in the database
        List<MedAuthorityEntity> medAuthorityList = medAuthorityRepository.findAll();
        assertThat(medAuthorityList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteMedAuthority() throws Exception {
        // Initialize the database
        medAuthorityRepository.saveAndFlush(medAuthorityEntity);

        int databaseSizeBeforeDelete = medAuthorityRepository.findAll().size();

        // Delete the medAuthority
        restMedAuthorityMockMvc
            .perform(delete(ENTITY_API_URL_ID, medAuthorityEntity.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<MedAuthorityEntity> medAuthorityList = medAuthorityRepository.findAll();
        assertThat(medAuthorityList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
*/
