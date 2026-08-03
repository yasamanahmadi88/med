package com.behsa.medportal.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.behsa.medportal.IntegrationTest;
import com.behsa.medportal.domain.VersionEntity;
import com.behsa.medportal.repository.VersionRepository;
import com.behsa.medportal.service.dto.VersionDTO;
import com.behsa.medportal.service.mapper.VersionMapper;
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
 * Integration tests for the {@link VersionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class VersionResourceIT {

    private static final String DEFAULT_TABLE_NAME = "AAAAAAAAAA";
    private static final String UPDATED_TABLE_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_MODULE_NAME = "AAAAAAAAAA";
    private static final String UPDATED_MODULE_NAME = "BBBBBBBBBB";

    private static final Long DEFAULT_TABLE_VERSION = 1L;
    private static final Long UPDATED_TABLE_VERSION = 2L;
    private static final Long SMALLER_TABLE_VERSION = 1L - 1L;

    private static final String ENTITY_API_URL = "/api/versions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private VersionRepository versionRepository;

    @Autowired
    private VersionMapper versionMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restVersionMockMvc;

    private VersionEntity versionEntity;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static VersionEntity createEntity(EntityManager em) {
        VersionEntity versionEntity = new VersionEntity()
            .tableName(DEFAULT_TABLE_NAME)
            .moduleName(DEFAULT_MODULE_NAME)
            .tableVersion(DEFAULT_TABLE_VERSION);
        return versionEntity;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static VersionEntity createUpdatedEntity(EntityManager em) {
        VersionEntity versionEntity = new VersionEntity()
            .tableName(UPDATED_TABLE_NAME)
            .moduleName(UPDATED_MODULE_NAME)
            .tableVersion(UPDATED_TABLE_VERSION);
        return versionEntity;
    }

    @BeforeEach
    public void initTest() {
        versionEntity = createEntity(em);
    }

    @Test
    @Transactional
    void createVersion() throws Exception {
        int databaseSizeBeforeCreate = versionRepository.findAll().size();
        // Create the Version
        VersionDTO versionDTO = versionMapper.toDto(versionEntity);
        restVersionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(versionDTO)))
            .andExpect(status().isCreated());

        // Validate the Version in the database
        List<VersionEntity> versionList = versionRepository.findAll();
        assertThat(versionList).hasSize(databaseSizeBeforeCreate + 1);
        VersionEntity testVersion = versionList.get(versionList.size() - 1);
        assertThat(testVersion.getTableName()).isEqualTo(DEFAULT_TABLE_NAME);
        assertThat(testVersion.getModuleName()).isEqualTo(DEFAULT_MODULE_NAME);
        assertThat(testVersion.getTableVersion()).isEqualTo(DEFAULT_TABLE_VERSION);
    }

    @Test
    @Transactional
    void createVersionWithExistingId() throws Exception {
        // Create the Version with an existing ID
        versionEntity.setId(1L);
        VersionDTO versionDTO = versionMapper.toDto(versionEntity);

        int databaseSizeBeforeCreate = versionRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restVersionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(versionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Version in the database
        List<VersionEntity> versionList = versionRepository.findAll();
        assertThat(versionList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTableNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = versionRepository.findAll().size();
        // set the field null
        versionEntity.setTableName(null);

        // Create the Version, which fails.
        VersionDTO versionDTO = versionMapper.toDto(versionEntity);

        restVersionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(versionDTO)))
            .andExpect(status().isBadRequest());

        List<VersionEntity> versionList = versionRepository.findAll();
        assertThat(versionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkModuleNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = versionRepository.findAll().size();
        // set the field null
        versionEntity.setModuleName(null);

        // Create the Version, which fails.
        VersionDTO versionDTO = versionMapper.toDto(versionEntity);

        restVersionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(versionDTO)))
            .andExpect(status().isBadRequest());

        List<VersionEntity> versionList = versionRepository.findAll();
        assertThat(versionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTableVersionIsRequired() throws Exception {
        int databaseSizeBeforeTest = versionRepository.findAll().size();
        // set the field null
        versionEntity.setTableVersion(null);

        // Create the Version, which fails.
        VersionDTO versionDTO = versionMapper.toDto(versionEntity);

        restVersionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(versionDTO)))
            .andExpect(status().isBadRequest());

        List<VersionEntity> versionList = versionRepository.findAll();
        assertThat(versionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllVersions() throws Exception {
        // Initialize the database
        versionRepository.saveAndFlush(versionEntity);

        // Get all the versionList
        restVersionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(versionEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].tableName").value(hasItem(DEFAULT_TABLE_NAME)))
            .andExpect(jsonPath("$.[*].moduleName").value(hasItem(DEFAULT_MODULE_NAME)))
            .andExpect(jsonPath("$.[*].tableVersion").value(hasItem(DEFAULT_TABLE_VERSION.intValue())));
    }

    @Test
    @Transactional
    void getVersion() throws Exception {
        // Initialize the database
        versionRepository.saveAndFlush(versionEntity);

        // Get the version
        restVersionMockMvc
            .perform(get(ENTITY_API_URL_ID, versionEntity.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(versionEntity.getId().intValue()))
            .andExpect(jsonPath("$.tableName").value(DEFAULT_TABLE_NAME))
            .andExpect(jsonPath("$.moduleName").value(DEFAULT_MODULE_NAME))
            .andExpect(jsonPath("$.tableVersion").value(DEFAULT_TABLE_VERSION.intValue()));
    }

    @Test
    @Transactional
    void getVersionsByIdFiltering() throws Exception {
        // Initialize the database
        versionRepository.saveAndFlush(versionEntity);

        Long id = versionEntity.getId();

        defaultVersionShouldBeFound("id.equals=" + id);
        defaultVersionShouldNotBeFound("id.notEquals=" + id);

        defaultVersionShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultVersionShouldNotBeFound("id.greaterThan=" + id);

        defaultVersionShouldBeFound("id.lessThanOrEqual=" + id);
        defaultVersionShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllVersionsByTableNameIsEqualToSomething() throws Exception {
        // Initialize the database
        versionRepository.saveAndFlush(versionEntity);

        // Get all the versionList where tableName equals to DEFAULT_TABLE_NAME
        defaultVersionShouldBeFound("tableName.equals=" + DEFAULT_TABLE_NAME);

        // Get all the versionList where tableName equals to UPDATED_TABLE_NAME
        defaultVersionShouldNotBeFound("tableName.equals=" + UPDATED_TABLE_NAME);
    }

    @Test
    @Transactional
    void getAllVersionsByTableNameIsInShouldWork() throws Exception {
        // Initialize the database
        versionRepository.saveAndFlush(versionEntity);

        // Get all the versionList where tableName in DEFAULT_TABLE_NAME or UPDATED_TABLE_NAME
        defaultVersionShouldBeFound("tableName.in=" + DEFAULT_TABLE_NAME + "," + UPDATED_TABLE_NAME);

        // Get all the versionList where tableName equals to UPDATED_TABLE_NAME
        defaultVersionShouldNotBeFound("tableName.in=" + UPDATED_TABLE_NAME);
    }

    @Test
    @Transactional
    void getAllVersionsByTableNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        versionRepository.saveAndFlush(versionEntity);

        // Get all the versionList where tableName is not null
        defaultVersionShouldBeFound("tableName.specified=true");

        // Get all the versionList where tableName is null
        defaultVersionShouldNotBeFound("tableName.specified=false");
    }

    @Test
    @Transactional
    void getAllVersionsByTableNameContainsSomething() throws Exception {
        // Initialize the database
        versionRepository.saveAndFlush(versionEntity);

        // Get all the versionList where tableName contains DEFAULT_TABLE_NAME
        defaultVersionShouldBeFound("tableName.contains=" + DEFAULT_TABLE_NAME);

        // Get all the versionList where tableName contains UPDATED_TABLE_NAME
        defaultVersionShouldNotBeFound("tableName.contains=" + UPDATED_TABLE_NAME);
    }

    @Test
    @Transactional
    void getAllVersionsByTableNameNotContainsSomething() throws Exception {
        // Initialize the database
        versionRepository.saveAndFlush(versionEntity);

        // Get all the versionList where tableName does not contain DEFAULT_TABLE_NAME
        defaultVersionShouldNotBeFound("tableName.doesNotContain=" + DEFAULT_TABLE_NAME);

        // Get all the versionList where tableName does not contain UPDATED_TABLE_NAME
        defaultVersionShouldBeFound("tableName.doesNotContain=" + UPDATED_TABLE_NAME);
    }

    @Test
    @Transactional
    void getAllVersionsByModuleNameIsEqualToSomething() throws Exception {
        // Initialize the database
        versionRepository.saveAndFlush(versionEntity);

        // Get all the versionList where moduleName equals to DEFAULT_MODULE_NAME
        defaultVersionShouldBeFound("moduleName.equals=" + DEFAULT_MODULE_NAME);

        // Get all the versionList where moduleName equals to UPDATED_MODULE_NAME
        defaultVersionShouldNotBeFound("moduleName.equals=" + UPDATED_MODULE_NAME);
    }

    @Test
    @Transactional
    void getAllVersionsByModuleNameIsInShouldWork() throws Exception {
        // Initialize the database
        versionRepository.saveAndFlush(versionEntity);

        // Get all the versionList where moduleName in DEFAULT_MODULE_NAME or UPDATED_MODULE_NAME
        defaultVersionShouldBeFound("moduleName.in=" + DEFAULT_MODULE_NAME + "," + UPDATED_MODULE_NAME);

        // Get all the versionList where moduleName equals to UPDATED_MODULE_NAME
        defaultVersionShouldNotBeFound("moduleName.in=" + UPDATED_MODULE_NAME);
    }

    @Test
    @Transactional
    void getAllVersionsByModuleNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        versionRepository.saveAndFlush(versionEntity);

        // Get all the versionList where moduleName is not null
        defaultVersionShouldBeFound("moduleName.specified=true");

        // Get all the versionList where moduleName is null
        defaultVersionShouldNotBeFound("moduleName.specified=false");
    }

    @Test
    @Transactional
    void getAllVersionsByModuleNameContainsSomething() throws Exception {
        // Initialize the database
        versionRepository.saveAndFlush(versionEntity);

        // Get all the versionList where moduleName contains DEFAULT_MODULE_NAME
        defaultVersionShouldBeFound("moduleName.contains=" + DEFAULT_MODULE_NAME);

        // Get all the versionList where moduleName contains UPDATED_MODULE_NAME
        defaultVersionShouldNotBeFound("moduleName.contains=" + UPDATED_MODULE_NAME);
    }

    @Test
    @Transactional
    void getAllVersionsByModuleNameNotContainsSomething() throws Exception {
        // Initialize the database
        versionRepository.saveAndFlush(versionEntity);

        // Get all the versionList where moduleName does not contain DEFAULT_MODULE_NAME
        defaultVersionShouldNotBeFound("moduleName.doesNotContain=" + DEFAULT_MODULE_NAME);

        // Get all the versionList where moduleName does not contain UPDATED_MODULE_NAME
        defaultVersionShouldBeFound("moduleName.doesNotContain=" + UPDATED_MODULE_NAME);
    }

    @Test
    @Transactional
    void getAllVersionsByTableVersionIsEqualToSomething() throws Exception {
        // Initialize the database
        versionRepository.saveAndFlush(versionEntity);

        // Get all the versionList where tableVersion equals to DEFAULT_TABLE_VERSION
        defaultVersionShouldBeFound("tableVersion.equals=" + DEFAULT_TABLE_VERSION);

        // Get all the versionList where tableVersion equals to UPDATED_TABLE_VERSION
        defaultVersionShouldNotBeFound("tableVersion.equals=" + UPDATED_TABLE_VERSION);
    }

    @Test
    @Transactional
    void getAllVersionsByTableVersionIsInShouldWork() throws Exception {
        // Initialize the database
        versionRepository.saveAndFlush(versionEntity);

        // Get all the versionList where tableVersion in DEFAULT_TABLE_VERSION or UPDATED_TABLE_VERSION
        defaultVersionShouldBeFound("tableVersion.in=" + DEFAULT_TABLE_VERSION + "," + UPDATED_TABLE_VERSION);

        // Get all the versionList where tableVersion equals to UPDATED_TABLE_VERSION
        defaultVersionShouldNotBeFound("tableVersion.in=" + UPDATED_TABLE_VERSION);
    }

    @Test
    @Transactional
    void getAllVersionsByTableVersionIsNullOrNotNull() throws Exception {
        // Initialize the database
        versionRepository.saveAndFlush(versionEntity);

        // Get all the versionList where tableVersion is not null
        defaultVersionShouldBeFound("tableVersion.specified=true");

        // Get all the versionList where tableVersion is null
        defaultVersionShouldNotBeFound("tableVersion.specified=false");
    }

    @Test
    @Transactional
    void getAllVersionsByTableVersionIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        versionRepository.saveAndFlush(versionEntity);

        // Get all the versionList where tableVersion is greater than or equal to DEFAULT_TABLE_VERSION
        defaultVersionShouldBeFound("tableVersion.greaterThanOrEqual=" + DEFAULT_TABLE_VERSION);

        // Get all the versionList where tableVersion is greater than or equal to UPDATED_TABLE_VERSION
        defaultVersionShouldNotBeFound("tableVersion.greaterThanOrEqual=" + UPDATED_TABLE_VERSION);
    }

    @Test
    @Transactional
    void getAllVersionsByTableVersionIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        versionRepository.saveAndFlush(versionEntity);

        // Get all the versionList where tableVersion is less than or equal to DEFAULT_TABLE_VERSION
        defaultVersionShouldBeFound("tableVersion.lessThanOrEqual=" + DEFAULT_TABLE_VERSION);

        // Get all the versionList where tableVersion is less than or equal to SMALLER_TABLE_VERSION
        defaultVersionShouldNotBeFound("tableVersion.lessThanOrEqual=" + SMALLER_TABLE_VERSION);
    }

    @Test
    @Transactional
    void getAllVersionsByTableVersionIsLessThanSomething() throws Exception {
        // Initialize the database
        versionRepository.saveAndFlush(versionEntity);

        // Get all the versionList where tableVersion is less than DEFAULT_TABLE_VERSION
        defaultVersionShouldNotBeFound("tableVersion.lessThan=" + DEFAULT_TABLE_VERSION);

        // Get all the versionList where tableVersion is less than UPDATED_TABLE_VERSION
        defaultVersionShouldBeFound("tableVersion.lessThan=" + UPDATED_TABLE_VERSION);
    }

    @Test
    @Transactional
    void getAllVersionsByTableVersionIsGreaterThanSomething() throws Exception {
        // Initialize the database
        versionRepository.saveAndFlush(versionEntity);

        // Get all the versionList where tableVersion is greater than DEFAULT_TABLE_VERSION
        defaultVersionShouldNotBeFound("tableVersion.greaterThan=" + DEFAULT_TABLE_VERSION);

        // Get all the versionList where tableVersion is greater than SMALLER_TABLE_VERSION
        defaultVersionShouldBeFound("tableVersion.greaterThan=" + SMALLER_TABLE_VERSION);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultVersionShouldBeFound(String filter) throws Exception {
        restVersionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(versionEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].tableName").value(hasItem(DEFAULT_TABLE_NAME)))
            .andExpect(jsonPath("$.[*].moduleName").value(hasItem(DEFAULT_MODULE_NAME)))
            .andExpect(jsonPath("$.[*].tableVersion").value(hasItem(DEFAULT_TABLE_VERSION.intValue())));

        // Check, that the count call also returns 1
        restVersionMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultVersionShouldNotBeFound(String filter) throws Exception {
        restVersionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restVersionMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingVersion() throws Exception {
        // Get the version
        restVersionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingVersion() throws Exception {
        // Initialize the database
        versionRepository.saveAndFlush(versionEntity);

        int databaseSizeBeforeUpdate = versionRepository.findAll().size();

        // Update the version
        VersionEntity updatedVersionEntity = versionRepository.findById(versionEntity.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedVersionEntity are not directly saved in db
        em.detach(updatedVersionEntity);
        updatedVersionEntity.tableName(UPDATED_TABLE_NAME).moduleName(UPDATED_MODULE_NAME).tableVersion(UPDATED_TABLE_VERSION);
        VersionDTO versionDTO = versionMapper.toDto(updatedVersionEntity);

        restVersionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, versionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(versionDTO))
            )
            .andExpect(status().isOk());

        // Validate the Version in the database
        List<VersionEntity> versionList = versionRepository.findAll();
        assertThat(versionList).hasSize(databaseSizeBeforeUpdate);
        VersionEntity testVersion = versionList.get(versionList.size() - 1);
        assertThat(testVersion.getTableName()).isEqualTo(UPDATED_TABLE_NAME);
        assertThat(testVersion.getModuleName()).isEqualTo(UPDATED_MODULE_NAME);
        assertThat(testVersion.getTableVersion()).isEqualTo(UPDATED_TABLE_VERSION);
    }

    @Test
    @Transactional
    void putNonExistingVersion() throws Exception {
        int databaseSizeBeforeUpdate = versionRepository.findAll().size();
        versionEntity.setId(count.incrementAndGet());

        // Create the Version
        VersionDTO versionDTO = versionMapper.toDto(versionEntity);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restVersionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, versionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(versionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Version in the database
        List<VersionEntity> versionList = versionRepository.findAll();
        assertThat(versionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchVersion() throws Exception {
        int databaseSizeBeforeUpdate = versionRepository.findAll().size();
        versionEntity.setId(count.incrementAndGet());

        // Create the Version
        VersionDTO versionDTO = versionMapper.toDto(versionEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVersionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(versionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Version in the database
        List<VersionEntity> versionList = versionRepository.findAll();
        assertThat(versionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamVersion() throws Exception {
        int databaseSizeBeforeUpdate = versionRepository.findAll().size();
        versionEntity.setId(count.incrementAndGet());

        // Create the Version
        VersionDTO versionDTO = versionMapper.toDto(versionEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVersionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(versionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Version in the database
        List<VersionEntity> versionList = versionRepository.findAll();
        assertThat(versionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateVersionWithPatch() throws Exception {
        // Initialize the database
        versionRepository.saveAndFlush(versionEntity);

        int databaseSizeBeforeUpdate = versionRepository.findAll().size();

        // Update the version using partial update
        VersionEntity partialUpdatedVersionEntity = new VersionEntity();
        partialUpdatedVersionEntity.setId(versionEntity.getId());

        partialUpdatedVersionEntity.tableName(UPDATED_TABLE_NAME);

        restVersionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedVersionEntity.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedVersionEntity))
            )
            .andExpect(status().isOk());

        // Validate the Version in the database
        List<VersionEntity> versionList = versionRepository.findAll();
        assertThat(versionList).hasSize(databaseSizeBeforeUpdate);
        VersionEntity testVersion = versionList.get(versionList.size() - 1);
        assertThat(testVersion.getTableName()).isEqualTo(UPDATED_TABLE_NAME);
        assertThat(testVersion.getModuleName()).isEqualTo(DEFAULT_MODULE_NAME);
        assertThat(testVersion.getTableVersion()).isEqualTo(DEFAULT_TABLE_VERSION);
    }

    @Test
    @Transactional
    void fullUpdateVersionWithPatch() throws Exception {
        // Initialize the database
        versionRepository.saveAndFlush(versionEntity);

        int databaseSizeBeforeUpdate = versionRepository.findAll().size();

        // Update the version using partial update
        VersionEntity partialUpdatedVersionEntity = new VersionEntity();
        partialUpdatedVersionEntity.setId(versionEntity.getId());

        partialUpdatedVersionEntity.tableName(UPDATED_TABLE_NAME).moduleName(UPDATED_MODULE_NAME).tableVersion(UPDATED_TABLE_VERSION);

        restVersionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedVersionEntity.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedVersionEntity))
            )
            .andExpect(status().isOk());

        // Validate the Version in the database
        List<VersionEntity> versionList = versionRepository.findAll();
        assertThat(versionList).hasSize(databaseSizeBeforeUpdate);
        VersionEntity testVersion = versionList.get(versionList.size() - 1);
        assertThat(testVersion.getTableName()).isEqualTo(UPDATED_TABLE_NAME);
        assertThat(testVersion.getModuleName()).isEqualTo(UPDATED_MODULE_NAME);
        assertThat(testVersion.getTableVersion()).isEqualTo(UPDATED_TABLE_VERSION);
    }

    @Test
    @Transactional
    void patchNonExistingVersion() throws Exception {
        int databaseSizeBeforeUpdate = versionRepository.findAll().size();
        versionEntity.setId(count.incrementAndGet());

        // Create the Version
        VersionDTO versionDTO = versionMapper.toDto(versionEntity);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restVersionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, versionDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(versionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Version in the database
        List<VersionEntity> versionList = versionRepository.findAll();
        assertThat(versionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchVersion() throws Exception {
        int databaseSizeBeforeUpdate = versionRepository.findAll().size();
        versionEntity.setId(count.incrementAndGet());

        // Create the Version
        VersionDTO versionDTO = versionMapper.toDto(versionEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVersionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(versionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Version in the database
        List<VersionEntity> versionList = versionRepository.findAll();
        assertThat(versionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamVersion() throws Exception {
        int databaseSizeBeforeUpdate = versionRepository.findAll().size();
        versionEntity.setId(count.incrementAndGet());

        // Create the Version
        VersionDTO versionDTO = versionMapper.toDto(versionEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVersionMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(versionDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Version in the database
        List<VersionEntity> versionList = versionRepository.findAll();
        assertThat(versionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteVersion() throws Exception {
        // Initialize the database
        versionRepository.saveAndFlush(versionEntity);

        int databaseSizeBeforeDelete = versionRepository.findAll().size();

        // Delete the version
        restVersionMockMvc
            .perform(delete(ENTITY_API_URL_ID, versionEntity.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<VersionEntity> versionList = versionRepository.findAll();
        assertThat(versionList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
