package com.behsa.medportal.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.behsa.medportal.IntegrationTest;
import com.behsa.medportal.domain.ConfigEntity;
import com.behsa.medportal.domain.ModuleEntity;
import com.behsa.medportal.repository.ConfigRepository;
import com.behsa.medportal.service.dto.ConfigDTO;
import com.behsa.medportal.service.mapper.ConfigMapper;
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

/**
 * Integration tests for the {@link ConfigResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ConfigResourceIT {

    private static final String DEFAULT_PROPERTY = "AAAAAAAAAA";
    private static final String UPDATED_PROPERTY = "BBBBBBBBBB";

    private static final String DEFAULT_P_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_P_VALUE = "BBBBBBBBBB";

    private static final String DEFAULT_COMMENT_DESC = "AAAAAAAAAA";
    private static final String UPDATED_COMMENT_DESC = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/configs";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private ConfigMapper configMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restConfigMockMvc;

    private ConfigEntity configEntity;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ConfigEntity createEntity(EntityManager em) {
        ConfigEntity configEntity = new ConfigEntity().property(DEFAULT_PROPERTY).pValue(DEFAULT_P_VALUE).commentDesc(DEFAULT_COMMENT_DESC);
        // Add required entity
        ModuleEntity module;
        if (TestUtil.findAll(em, ModuleEntity.class).isEmpty()) {
            module = ModuleResourceIT.createEntity(em);
            em.persist(module);
            em.flush();
        } else {
            module = TestUtil.findAll(em, ModuleEntity.class).get(0);
        }
        configEntity.setModule(module);
        return configEntity;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ConfigEntity createUpdatedEntity(EntityManager em) {
        ConfigEntity configEntity = new ConfigEntity().property(UPDATED_PROPERTY).pValue(UPDATED_P_VALUE).commentDesc(UPDATED_COMMENT_DESC);
        // Add required entity
        ModuleEntity module;
        if (TestUtil.findAll(em, ModuleEntity.class).isEmpty()) {
            module = ModuleResourceIT.createUpdatedEntity(em);
            em.persist(module);
            em.flush();
        } else {
            module = TestUtil.findAll(em, ModuleEntity.class).get(0);
        }
        configEntity.setModule(module);
        return configEntity;
    }

    @BeforeEach
    public void initTest() {
        configEntity = createEntity(em);
    }

    @Test
    @Transactional
    void createConfig() throws Exception {
        int databaseSizeBeforeCreate = configRepository.findAll().size();
        // Create the Config
        ConfigDTO configDTO = configMapper.toDto(configEntity);
        restConfigMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(configDTO)))
            .andExpect(status().isCreated());

        // Validate the Config in the database
        List<ConfigEntity> configList = configRepository.findAll();
        assertThat(configList).hasSize(databaseSizeBeforeCreate + 1);
        ConfigEntity testConfig = configList.get(configList.size() - 1);
        assertThat(testConfig.getProperty()).isEqualTo(DEFAULT_PROPERTY);
        assertThat(testConfig.getpValue()).isEqualTo(DEFAULT_P_VALUE);
        assertThat(testConfig.getCommentDesc()).isEqualTo(DEFAULT_COMMENT_DESC);
    }

    @Test
    @Transactional
    void createConfigWithExistingId() throws Exception {
        // Create the Config with an existing ID
        configEntity.setId(1L);
        ConfigDTO configDTO = configMapper.toDto(configEntity);

        int databaseSizeBeforeCreate = configRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restConfigMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(configDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Config in the database
        List<ConfigEntity> configList = configRepository.findAll();
        assertThat(configList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkPropertyIsRequired() throws Exception {
        int databaseSizeBeforeTest = configRepository.findAll().size();
        // set the field null
        configEntity.setProperty(null);

        // Create the Config, which fails.
        ConfigDTO configDTO = configMapper.toDto(configEntity);

        restConfigMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(configDTO)))
            .andExpect(status().isBadRequest());

        List<ConfigEntity> configList = configRepository.findAll();
        assertThat(configList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkpValueIsRequired() throws Exception {
        int databaseSizeBeforeTest = configRepository.findAll().size();
        // set the field null
        configEntity.setpValue(null);

        // Create the Config, which fails.
        ConfigDTO configDTO = configMapper.toDto(configEntity);

        restConfigMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(configDTO)))
            .andExpect(status().isBadRequest());

        List<ConfigEntity> configList = configRepository.findAll();
        assertThat(configList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllConfigs() throws Exception {
        // Initialize the database
        configRepository.saveAndFlush(configEntity);

        // Get all the configList
        restConfigMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(configEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].property").value(hasItem(DEFAULT_PROPERTY)))
            .andExpect(jsonPath("$.[*].pValue").value(hasItem(DEFAULT_P_VALUE)))
            .andExpect(jsonPath("$.[*].commentDesc").value(hasItem(DEFAULT_COMMENT_DESC)));
    }

    @Test
    @Transactional
    void getConfig() throws Exception {
        // Initialize the database
        configRepository.saveAndFlush(configEntity);

        // Get the config
        restConfigMockMvc
            .perform(get(ENTITY_API_URL_ID, configEntity.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(configEntity.getId().intValue()))
            .andExpect(jsonPath("$.property").value(DEFAULT_PROPERTY))
            .andExpect(jsonPath("$.pValue").value(DEFAULT_P_VALUE))
            .andExpect(jsonPath("$.commentDesc").value(DEFAULT_COMMENT_DESC));
    }

    @Test
    @Transactional
    void getConfigsByIdFiltering() throws Exception {
        // Initialize the database
        configRepository.saveAndFlush(configEntity);

        Long id = configEntity.getId();

        defaultConfigShouldBeFound("id.equals=" + id);
        defaultConfigShouldNotBeFound("id.notEquals=" + id);

        defaultConfigShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultConfigShouldNotBeFound("id.greaterThan=" + id);

        defaultConfigShouldBeFound("id.lessThanOrEqual=" + id);
        defaultConfigShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllConfigsByPropertyIsEqualToSomething() throws Exception {
        // Initialize the database
        configRepository.saveAndFlush(configEntity);

        // Get all the configList where property equals to DEFAULT_PROPERTY
        defaultConfigShouldBeFound("property.equals=" + DEFAULT_PROPERTY);

        // Get all the configList where property equals to UPDATED_PROPERTY
        defaultConfigShouldNotBeFound("property.equals=" + UPDATED_PROPERTY);
    }

    @Test
    @Transactional
    void getAllConfigsByPropertyIsInShouldWork() throws Exception {
        // Initialize the database
        configRepository.saveAndFlush(configEntity);

        // Get all the configList where property in DEFAULT_PROPERTY or UPDATED_PROPERTY
        defaultConfigShouldBeFound("property.in=" + DEFAULT_PROPERTY + "," + UPDATED_PROPERTY);

        // Get all the configList where property equals to UPDATED_PROPERTY
        defaultConfigShouldNotBeFound("property.in=" + UPDATED_PROPERTY);
    }

    @Test
    @Transactional
    void getAllConfigsByPropertyIsNullOrNotNull() throws Exception {
        // Initialize the database
        configRepository.saveAndFlush(configEntity);

        // Get all the configList where property is not null
        defaultConfigShouldBeFound("property.specified=true");

        // Get all the configList where property is null
        defaultConfigShouldNotBeFound("property.specified=false");
    }

    @Test
    @Transactional
    void getAllConfigsByPropertyContainsSomething() throws Exception {
        // Initialize the database
        configRepository.saveAndFlush(configEntity);

        // Get all the configList where property contains DEFAULT_PROPERTY
        defaultConfigShouldBeFound("property.contains=" + DEFAULT_PROPERTY);

        // Get all the configList where property contains UPDATED_PROPERTY
        defaultConfigShouldNotBeFound("property.contains=" + UPDATED_PROPERTY);
    }

    @Test
    @Transactional
    void getAllConfigsByPropertyNotContainsSomething() throws Exception {
        // Initialize the database
        configRepository.saveAndFlush(configEntity);

        // Get all the configList where property does not contain DEFAULT_PROPERTY
        defaultConfigShouldNotBeFound("property.doesNotContain=" + DEFAULT_PROPERTY);

        // Get all the configList where property does not contain UPDATED_PROPERTY
        defaultConfigShouldBeFound("property.doesNotContain=" + UPDATED_PROPERTY);
    }

    @Test
    @Transactional
    void getAllConfigsBypValueIsEqualToSomething() throws Exception {
        // Initialize the database
        configRepository.saveAndFlush(configEntity);

        // Get all the configList where pValue equals to DEFAULT_P_VALUE
        defaultConfigShouldBeFound("pValue.equals=" + DEFAULT_P_VALUE);

        // Get all the configList where pValue equals to UPDATED_P_VALUE
        defaultConfigShouldNotBeFound("pValue.equals=" + UPDATED_P_VALUE);
    }

    @Test
    @Transactional
    void getAllConfigsBypValueIsInShouldWork() throws Exception {
        // Initialize the database
        configRepository.saveAndFlush(configEntity);

        // Get all the configList where pValue in DEFAULT_P_VALUE or UPDATED_P_VALUE
        defaultConfigShouldBeFound("pValue.in=" + DEFAULT_P_VALUE + "," + UPDATED_P_VALUE);

        // Get all the configList where pValue equals to UPDATED_P_VALUE
        defaultConfigShouldNotBeFound("pValue.in=" + UPDATED_P_VALUE);
    }

    @Test
    @Transactional
    void getAllConfigsBypValueIsNullOrNotNull() throws Exception {
        // Initialize the database
        configRepository.saveAndFlush(configEntity);

        // Get all the configList where pValue is not null
        defaultConfigShouldBeFound("pValue.specified=true");

        // Get all the configList where pValue is null
        defaultConfigShouldNotBeFound("pValue.specified=false");
    }

    @Test
    @Transactional
    void getAllConfigsBypValueContainsSomething() throws Exception {
        // Initialize the database
        configRepository.saveAndFlush(configEntity);

        // Get all the configList where pValue contains DEFAULT_P_VALUE
        defaultConfigShouldBeFound("pValue.contains=" + DEFAULT_P_VALUE);

        // Get all the configList where pValue contains UPDATED_P_VALUE
        defaultConfigShouldNotBeFound("pValue.contains=" + UPDATED_P_VALUE);
    }

    @Test
    @Transactional
    void getAllConfigsBypValueNotContainsSomething() throws Exception {
        // Initialize the database
        configRepository.saveAndFlush(configEntity);

        // Get all the configList where pValue does not contain DEFAULT_P_VALUE
        defaultConfigShouldNotBeFound("pValue.doesNotContain=" + DEFAULT_P_VALUE);

        // Get all the configList where pValue does not contain UPDATED_P_VALUE
        defaultConfigShouldBeFound("pValue.doesNotContain=" + UPDATED_P_VALUE);
    }

    @Test
    @Transactional
    void getAllConfigsByCommentDescIsEqualToSomething() throws Exception {
        // Initialize the database
        configRepository.saveAndFlush(configEntity);

        // Get all the configList where commentDesc equals to DEFAULT_COMMENT_DESC
        defaultConfigShouldBeFound("commentDesc.equals=" + DEFAULT_COMMENT_DESC);

        // Get all the configList where commentDesc equals to UPDATED_COMMENT_DESC
        defaultConfigShouldNotBeFound("commentDesc.equals=" + UPDATED_COMMENT_DESC);
    }

    @Test
    @Transactional
    void getAllConfigsByCommentDescIsInShouldWork() throws Exception {
        // Initialize the database
        configRepository.saveAndFlush(configEntity);

        // Get all the configList where commentDesc in DEFAULT_COMMENT_DESC or UPDATED_COMMENT_DESC
        defaultConfigShouldBeFound("commentDesc.in=" + DEFAULT_COMMENT_DESC + "," + UPDATED_COMMENT_DESC);

        // Get all the configList where commentDesc equals to UPDATED_COMMENT_DESC
        defaultConfigShouldNotBeFound("commentDesc.in=" + UPDATED_COMMENT_DESC);
    }

    @Test
    @Transactional
    void getAllConfigsByCommentDescIsNullOrNotNull() throws Exception {
        // Initialize the database
        configRepository.saveAndFlush(configEntity);

        // Get all the configList where commentDesc is not null
        defaultConfigShouldBeFound("commentDesc.specified=true");

        // Get all the configList where commentDesc is null
        defaultConfigShouldNotBeFound("commentDesc.specified=false");
    }

    @Test
    @Transactional
    void getAllConfigsByCommentDescContainsSomething() throws Exception {
        // Initialize the database
        configRepository.saveAndFlush(configEntity);

        // Get all the configList where commentDesc contains DEFAULT_COMMENT_DESC
        defaultConfigShouldBeFound("commentDesc.contains=" + DEFAULT_COMMENT_DESC);

        // Get all the configList where commentDesc contains UPDATED_COMMENT_DESC
        defaultConfigShouldNotBeFound("commentDesc.contains=" + UPDATED_COMMENT_DESC);
    }

    @Test
    @Transactional
    void getAllConfigsByCommentDescNotContainsSomething() throws Exception {
        // Initialize the database
        configRepository.saveAndFlush(configEntity);

        // Get all the configList where commentDesc does not contain DEFAULT_COMMENT_DESC
        defaultConfigShouldNotBeFound("commentDesc.doesNotContain=" + DEFAULT_COMMENT_DESC);

        // Get all the configList where commentDesc does not contain UPDATED_COMMENT_DESC
        defaultConfigShouldBeFound("commentDesc.doesNotContain=" + UPDATED_COMMENT_DESC);
    }

    @Test
    @Transactional
    void getAllConfigsByModuleIsEqualToSomething() throws Exception {
        ModuleEntity module;
        if (TestUtil.findAll(em, ModuleEntity.class).isEmpty()) {
            configRepository.saveAndFlush(configEntity);
            module = ModuleResourceIT.createEntity(em);
        } else {
            module = TestUtil.findAll(em, ModuleEntity.class).get(0);
        }
        em.persist(module);
        em.flush();
        configEntity.setModule(module);
        configRepository.saveAndFlush(configEntity);
        Long moduleId = module.getId();

        // Get all the configList where module equals to moduleId
        defaultConfigShouldBeFound("moduleId.equals=" + moduleId);

        // Get all the configList where module equals to (moduleId + 1)
        defaultConfigShouldNotBeFound("moduleId.equals=" + (moduleId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultConfigShouldBeFound(String filter) throws Exception {
        restConfigMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(configEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].property").value(hasItem(DEFAULT_PROPERTY)))
            .andExpect(jsonPath("$.[*].pValue").value(hasItem(DEFAULT_P_VALUE)))
            .andExpect(jsonPath("$.[*].commentDesc").value(hasItem(DEFAULT_COMMENT_DESC)));

        // Check, that the count call also returns 1
        restConfigMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultConfigShouldNotBeFound(String filter) throws Exception {
        restConfigMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restConfigMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingConfig() throws Exception {
        // Get the config
        restConfigMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingConfig() throws Exception {
        // Initialize the database
        configRepository.saveAndFlush(configEntity);

        int databaseSizeBeforeUpdate = configRepository.findAll().size();

        // Update the config
        ConfigEntity updatedConfigEntity = configRepository.findById(configEntity.getId()).get();
        // Disconnect from session so that the updates on updatedConfigEntity are not directly saved in db
        em.detach(updatedConfigEntity);
        updatedConfigEntity.property(UPDATED_PROPERTY).pValue(UPDATED_P_VALUE).commentDesc(UPDATED_COMMENT_DESC);
        ConfigDTO configDTO = configMapper.toDto(updatedConfigEntity);

        restConfigMockMvc
            .perform(
                put(ENTITY_API_URL_ID, configDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(configDTO))
            )
            .andExpect(status().isOk());

        // Validate the Config in the database
        List<ConfigEntity> configList = configRepository.findAll();
        assertThat(configList).hasSize(databaseSizeBeforeUpdate);
        ConfigEntity testConfig = configList.get(configList.size() - 1);
        assertThat(testConfig.getProperty()).isEqualTo(UPDATED_PROPERTY);
        assertThat(testConfig.getpValue()).isEqualTo(UPDATED_P_VALUE);
        assertThat(testConfig.getCommentDesc()).isEqualTo(UPDATED_COMMENT_DESC);
    }

    @Test
    @Transactional
    void putNonExistingConfig() throws Exception {
        int databaseSizeBeforeUpdate = configRepository.findAll().size();
        configEntity.setId(count.incrementAndGet());

        // Create the Config
        ConfigDTO configDTO = configMapper.toDto(configEntity);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restConfigMockMvc
            .perform(
                put(ENTITY_API_URL_ID, configDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(configDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Config in the database
        List<ConfigEntity> configList = configRepository.findAll();
        assertThat(configList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchConfig() throws Exception {
        int databaseSizeBeforeUpdate = configRepository.findAll().size();
        configEntity.setId(count.incrementAndGet());

        // Create the Config
        ConfigDTO configDTO = configMapper.toDto(configEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restConfigMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(configDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Config in the database
        List<ConfigEntity> configList = configRepository.findAll();
        assertThat(configList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamConfig() throws Exception {
        int databaseSizeBeforeUpdate = configRepository.findAll().size();
        configEntity.setId(count.incrementAndGet());

        // Create the Config
        ConfigDTO configDTO = configMapper.toDto(configEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restConfigMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(configDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Config in the database
        List<ConfigEntity> configList = configRepository.findAll();
        assertThat(configList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateConfigWithPatch() throws Exception {
        // Initialize the database
        configRepository.saveAndFlush(configEntity);

        int databaseSizeBeforeUpdate = configRepository.findAll().size();

        // Update the config using partial update
        ConfigEntity partialUpdatedConfigEntity = new ConfigEntity();
        partialUpdatedConfigEntity.setId(configEntity.getId());

        partialUpdatedConfigEntity.property(UPDATED_PROPERTY).pValue(UPDATED_P_VALUE);

        restConfigMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedConfigEntity.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedConfigEntity))
            )
            .andExpect(status().isOk());

        // Validate the Config in the database
        List<ConfigEntity> configList = configRepository.findAll();
        assertThat(configList).hasSize(databaseSizeBeforeUpdate);
        ConfigEntity testConfig = configList.get(configList.size() - 1);
        assertThat(testConfig.getProperty()).isEqualTo(UPDATED_PROPERTY);
        assertThat(testConfig.getpValue()).isEqualTo(UPDATED_P_VALUE);
        assertThat(testConfig.getCommentDesc()).isEqualTo(DEFAULT_COMMENT_DESC);
    }

    @Test
    @Transactional
    void fullUpdateConfigWithPatch() throws Exception {
        // Initialize the database
        configRepository.saveAndFlush(configEntity);

        int databaseSizeBeforeUpdate = configRepository.findAll().size();

        // Update the config using partial update
        ConfigEntity partialUpdatedConfigEntity = new ConfigEntity();
        partialUpdatedConfigEntity.setId(configEntity.getId());

        partialUpdatedConfigEntity.property(UPDATED_PROPERTY).pValue(UPDATED_P_VALUE).commentDesc(UPDATED_COMMENT_DESC);

        restConfigMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedConfigEntity.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedConfigEntity))
            )
            .andExpect(status().isOk());

        // Validate the Config in the database
        List<ConfigEntity> configList = configRepository.findAll();
        assertThat(configList).hasSize(databaseSizeBeforeUpdate);
        ConfigEntity testConfig = configList.get(configList.size() - 1);
        assertThat(testConfig.getProperty()).isEqualTo(UPDATED_PROPERTY);
        assertThat(testConfig.getpValue()).isEqualTo(UPDATED_P_VALUE);
        assertThat(testConfig.getCommentDesc()).isEqualTo(UPDATED_COMMENT_DESC);
    }

    @Test
    @Transactional
    void patchNonExistingConfig() throws Exception {
        int databaseSizeBeforeUpdate = configRepository.findAll().size();
        configEntity.setId(count.incrementAndGet());

        // Create the Config
        ConfigDTO configDTO = configMapper.toDto(configEntity);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restConfigMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, configDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(configDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Config in the database
        List<ConfigEntity> configList = configRepository.findAll();
        assertThat(configList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchConfig() throws Exception {
        int databaseSizeBeforeUpdate = configRepository.findAll().size();
        configEntity.setId(count.incrementAndGet());

        // Create the Config
        ConfigDTO configDTO = configMapper.toDto(configEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restConfigMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(configDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Config in the database
        List<ConfigEntity> configList = configRepository.findAll();
        assertThat(configList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamConfig() throws Exception {
        int databaseSizeBeforeUpdate = configRepository.findAll().size();
        configEntity.setId(count.incrementAndGet());

        // Create the Config
        ConfigDTO configDTO = configMapper.toDto(configEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restConfigMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(configDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Config in the database
        List<ConfigEntity> configList = configRepository.findAll();
        assertThat(configList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteConfig() throws Exception {
        // Initialize the database
        configRepository.saveAndFlush(configEntity);

        int databaseSizeBeforeDelete = configRepository.findAll().size();

        // Delete the config
        restConfigMockMvc
            .perform(delete(ENTITY_API_URL_ID, configEntity.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<ConfigEntity> configList = configRepository.findAll();
        assertThat(configList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
