package com.behsa.medportal.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.behsa.medportal.IntegrationTest;
import com.behsa.medportal.domain.ConfigEntity;
import com.behsa.medportal.domain.ModuleEntity;
import com.behsa.medportal.repository.ModuleRepository;
import com.behsa.medportal.service.criteria.ModuleCriteria;
import com.behsa.medportal.service.dto.ModuleDTO;
import com.behsa.medportal.service.mapper.ModuleMapper;
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
 * Integration tests for the {@link ModuleResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ModuleResourceIT {

    private static final String DEFAULT_MODULE_NAME = "AAAAAAAAAA";
    private static final String UPDATED_MODULE_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DEFAULT_PORT = "AAAA";
    private static final String UPDATED_DEFAULT_PORT = "BBBB";

    private static final String DEFAULT_REDIS_KEY_PREFIX = "AAAAA";
    private static final String UPDATED_REDIS_KEY_PREFIX = "BBBBB";

    private static final Long DEFAULT_STATUS = 0L;
    private static final Long UPDATED_STATUS = 1L;
    private static final Long SMALLER_STATUS = 0L - 1L;

    private static final String DEFAULT_LOGGING_MODE = "AAAAAAAAAA";
    private static final String UPDATED_LOGGING_MODE = "BBBBBBBBBB";

    private static final String DEFAULT_LOGGING_FILTER = "AAAAAAAAAA";
    private static final String UPDATED_LOGGING_FILTER = "BBBBBBBBBB";

    private static final String DEFAULT_DNS_NAME = "AAAAAAAAAA";
    private static final String UPDATED_DNS_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/modules";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private ModuleMapper moduleMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restModuleMockMvc;

    private ModuleEntity moduleEntity;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ModuleEntity createEntity(EntityManager em) {
        ModuleEntity moduleEntity = new ModuleEntity()
            .moduleName(DEFAULT_MODULE_NAME)
            .defaultPort(DEFAULT_DEFAULT_PORT)
            .redisKeyPrefix(DEFAULT_REDIS_KEY_PREFIX)
            .status(DEFAULT_STATUS)
            .loggingMode(DEFAULT_LOGGING_MODE)
            .loggingFilter(DEFAULT_LOGGING_FILTER)
            .dnsName(DEFAULT_DNS_NAME);
        return moduleEntity;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ModuleEntity createUpdatedEntity(EntityManager em) {
        ModuleEntity moduleEntity = new ModuleEntity()
            .moduleName(UPDATED_MODULE_NAME)
            .defaultPort(UPDATED_DEFAULT_PORT)
            .redisKeyPrefix(UPDATED_REDIS_KEY_PREFIX)
            .status(UPDATED_STATUS)
            .loggingMode(UPDATED_LOGGING_MODE)
            .loggingFilter(UPDATED_LOGGING_FILTER)
            .dnsName(UPDATED_DNS_NAME);
        return moduleEntity;
    }

    @BeforeEach
    public void initTest() {
        moduleEntity = createEntity(em);
    }

    @Test
    @Transactional
    void createModule() throws Exception {
        int databaseSizeBeforeCreate = moduleRepository.findAll().size();
        // Create the Module
        ModuleDTO moduleDTO = moduleMapper.toDto(moduleEntity);
        restModuleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(moduleDTO)))
            .andExpect(status().isCreated());

        // Validate the Module in the database
        List<ModuleEntity> moduleList = moduleRepository.findAll();
        assertThat(moduleList).hasSize(databaseSizeBeforeCreate + 1);
        ModuleEntity testModule = moduleList.get(moduleList.size() - 1);
        assertThat(testModule.getModuleName()).isEqualTo(DEFAULT_MODULE_NAME);
        assertThat(testModule.getDefaultPort()).isEqualTo(DEFAULT_DEFAULT_PORT);
        assertThat(testModule.getRedisKeyPrefix()).isEqualTo(DEFAULT_REDIS_KEY_PREFIX);
        assertThat(testModule.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testModule.getLoggingMode()).isEqualTo(DEFAULT_LOGGING_MODE);
        assertThat(testModule.getLoggingFilter()).isEqualTo(DEFAULT_LOGGING_FILTER);
        assertThat(testModule.getDnsName()).isEqualTo(DEFAULT_DNS_NAME);
    }

    @Test
    @Transactional
    void createModuleWithExistingId() throws Exception {
        // Create the Module with an existing ID
        moduleEntity.setId(1L);
        ModuleDTO moduleDTO = moduleMapper.toDto(moduleEntity);

        int databaseSizeBeforeCreate = moduleRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restModuleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(moduleDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Module in the database
        List<ModuleEntity> moduleList = moduleRepository.findAll();
        assertThat(moduleList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkModuleNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = moduleRepository.findAll().size();
        // set the field null
        moduleEntity.setModuleName(null);

        // Create the Module, which fails.
        ModuleDTO moduleDTO = moduleMapper.toDto(moduleEntity);

        restModuleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(moduleDTO)))
            .andExpect(status().isBadRequest());

        List<ModuleEntity> moduleList = moduleRepository.findAll();
        assertThat(moduleList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkDefaultPortIsRequired() throws Exception {
        int databaseSizeBeforeTest = moduleRepository.findAll().size();
        // set the field null
        moduleEntity.setDefaultPort(null);

        // Create the Module, which fails.
        ModuleDTO moduleDTO = moduleMapper.toDto(moduleEntity);

        restModuleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(moduleDTO)))
            .andExpect(status().isBadRequest());

        List<ModuleEntity> moduleList = moduleRepository.findAll();
        assertThat(moduleList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkRedisKeyPrefixIsRequired() throws Exception {
        int databaseSizeBeforeTest = moduleRepository.findAll().size();
        // set the field null
        moduleEntity.setRedisKeyPrefix(null);

        // Create the Module, which fails.
        ModuleDTO moduleDTO = moduleMapper.toDto(moduleEntity);

        restModuleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(moduleDTO)))
            .andExpect(status().isBadRequest());

        List<ModuleEntity> moduleList = moduleRepository.findAll();
        assertThat(moduleList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        int databaseSizeBeforeTest = moduleRepository.findAll().size();
        // set the field null
        moduleEntity.setStatus(null);

        // Create the Module, which fails.
        ModuleDTO moduleDTO = moduleMapper.toDto(moduleEntity);

        restModuleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(moduleDTO)))
            .andExpect(status().isBadRequest());

        List<ModuleEntity> moduleList = moduleRepository.findAll();
        assertThat(moduleList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkLoggingModeIsRequired() throws Exception {
        int databaseSizeBeforeTest = moduleRepository.findAll().size();
        // set the field null
        moduleEntity.setLoggingMode(null);

        // Create the Module, which fails.
        ModuleDTO moduleDTO = moduleMapper.toDto(moduleEntity);

        restModuleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(moduleDTO)))
            .andExpect(status().isBadRequest());

        List<ModuleEntity> moduleList = moduleRepository.findAll();
        assertThat(moduleList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkDnsNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = moduleRepository.findAll().size();
        // set the field null
        moduleEntity.setDnsName(null);

        // Create the Module, which fails.
        ModuleDTO moduleDTO = moduleMapper.toDto(moduleEntity);

        restModuleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(moduleDTO)))
            .andExpect(status().isBadRequest());

        List<ModuleEntity> moduleList = moduleRepository.findAll();
        assertThat(moduleList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllModules() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList
        restModuleMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(moduleEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].moduleName").value(hasItem(DEFAULT_MODULE_NAME)))
            .andExpect(jsonPath("$.[*].defaultPort").value(hasItem(DEFAULT_DEFAULT_PORT)))
            .andExpect(jsonPath("$.[*].redisKeyPrefix").value(hasItem(DEFAULT_REDIS_KEY_PREFIX)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.intValue())))
            .andExpect(jsonPath("$.[*].loggingMode").value(hasItem(DEFAULT_LOGGING_MODE)))
            .andExpect(jsonPath("$.[*].loggingFilter").value(hasItem(DEFAULT_LOGGING_FILTER)))
            .andExpect(jsonPath("$.[*].dnsName").value(hasItem(DEFAULT_DNS_NAME)));
    }

    @Test
    @Transactional
    void getModule() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get the module
        restModuleMockMvc
            .perform(get(ENTITY_API_URL_ID, moduleEntity.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(moduleEntity.getId().intValue()))
            .andExpect(jsonPath("$.moduleName").value(DEFAULT_MODULE_NAME))
            .andExpect(jsonPath("$.defaultPort").value(DEFAULT_DEFAULT_PORT))
            .andExpect(jsonPath("$.redisKeyPrefix").value(DEFAULT_REDIS_KEY_PREFIX))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.intValue()))
            .andExpect(jsonPath("$.loggingMode").value(DEFAULT_LOGGING_MODE))
            .andExpect(jsonPath("$.loggingFilter").value(DEFAULT_LOGGING_FILTER))
            .andExpect(jsonPath("$.dnsName").value(DEFAULT_DNS_NAME));
    }

    @Test
    @Transactional
    void getModulesByIdFiltering() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        Long id = moduleEntity.getId();

        defaultModuleShouldBeFound("id.equals=" + id);
        defaultModuleShouldNotBeFound("id.notEquals=" + id);

        defaultModuleShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultModuleShouldNotBeFound("id.greaterThan=" + id);

        defaultModuleShouldBeFound("id.lessThanOrEqual=" + id);
        defaultModuleShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllModulesByModuleNameIsEqualToSomething() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where moduleName equals to DEFAULT_MODULE_NAME
        defaultModuleShouldBeFound("moduleName.equals=" + DEFAULT_MODULE_NAME);

        // Get all the moduleList where moduleName equals to UPDATED_MODULE_NAME
        defaultModuleShouldNotBeFound("moduleName.equals=" + UPDATED_MODULE_NAME);
    }

    @Test
    @Transactional
    void getAllModulesByModuleNameIsInShouldWork() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where moduleName in DEFAULT_MODULE_NAME or UPDATED_MODULE_NAME
        defaultModuleShouldBeFound("moduleName.in=" + DEFAULT_MODULE_NAME + "," + UPDATED_MODULE_NAME);

        // Get all the moduleList where moduleName equals to UPDATED_MODULE_NAME
        defaultModuleShouldNotBeFound("moduleName.in=" + UPDATED_MODULE_NAME);
    }

    @Test
    @Transactional
    void getAllModulesByModuleNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where moduleName is not null
        defaultModuleShouldBeFound("moduleName.specified=true");

        // Get all the moduleList where moduleName is null
        defaultModuleShouldNotBeFound("moduleName.specified=false");
    }

    @Test
    @Transactional
    void getAllModulesByModuleNameContainsSomething() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where moduleName contains DEFAULT_MODULE_NAME
        defaultModuleShouldBeFound("moduleName.contains=" + DEFAULT_MODULE_NAME);

        // Get all the moduleList where moduleName contains UPDATED_MODULE_NAME
        defaultModuleShouldNotBeFound("moduleName.contains=" + UPDATED_MODULE_NAME);
    }

    @Test
    @Transactional
    void getAllModulesByModuleNameNotContainsSomething() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where moduleName does not contain DEFAULT_MODULE_NAME
        defaultModuleShouldNotBeFound("moduleName.doesNotContain=" + DEFAULT_MODULE_NAME);

        // Get all the moduleList where moduleName does not contain UPDATED_MODULE_NAME
        defaultModuleShouldBeFound("moduleName.doesNotContain=" + UPDATED_MODULE_NAME);
    }

    @Test
    @Transactional
    void getAllModulesByDefaultPortIsEqualToSomething() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where defaultPort equals to DEFAULT_DEFAULT_PORT
        defaultModuleShouldBeFound("defaultPort.equals=" + DEFAULT_DEFAULT_PORT);

        // Get all the moduleList where defaultPort equals to UPDATED_DEFAULT_PORT
        defaultModuleShouldNotBeFound("defaultPort.equals=" + UPDATED_DEFAULT_PORT);
    }

    @Test
    @Transactional
    void getAllModulesByDefaultPortIsInShouldWork() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where defaultPort in DEFAULT_DEFAULT_PORT or UPDATED_DEFAULT_PORT
        defaultModuleShouldBeFound("defaultPort.in=" + DEFAULT_DEFAULT_PORT + "," + UPDATED_DEFAULT_PORT);

        // Get all the moduleList where defaultPort equals to UPDATED_DEFAULT_PORT
        defaultModuleShouldNotBeFound("defaultPort.in=" + UPDATED_DEFAULT_PORT);
    }

    @Test
    @Transactional
    void getAllModulesByDefaultPortIsNullOrNotNull() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where defaultPort is not null
        defaultModuleShouldBeFound("defaultPort.specified=true");

        // Get all the moduleList where defaultPort is null
        defaultModuleShouldNotBeFound("defaultPort.specified=false");
    }

    @Test
    @Transactional
    void getAllModulesByDefaultPortContainsSomething() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where defaultPort contains DEFAULT_DEFAULT_PORT
        defaultModuleShouldBeFound("defaultPort.contains=" + DEFAULT_DEFAULT_PORT);

        // Get all the moduleList where defaultPort contains UPDATED_DEFAULT_PORT
        defaultModuleShouldNotBeFound("defaultPort.contains=" + UPDATED_DEFAULT_PORT);
    }

    @Test
    @Transactional
    void getAllModulesByDefaultPortNotContainsSomething() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where defaultPort does not contain DEFAULT_DEFAULT_PORT
        defaultModuleShouldNotBeFound("defaultPort.doesNotContain=" + DEFAULT_DEFAULT_PORT);

        // Get all the moduleList where defaultPort does not contain UPDATED_DEFAULT_PORT
        defaultModuleShouldBeFound("defaultPort.doesNotContain=" + UPDATED_DEFAULT_PORT);
    }

    @Test
    @Transactional
    void getAllModulesByRedisKeyPrefixIsEqualToSomething() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where redisKeyPrefix equals to DEFAULT_REDIS_KEY_PREFIX
        defaultModuleShouldBeFound("redisKeyPrefix.equals=" + DEFAULT_REDIS_KEY_PREFIX);

        // Get all the moduleList where redisKeyPrefix equals to UPDATED_REDIS_KEY_PREFIX
        defaultModuleShouldNotBeFound("redisKeyPrefix.equals=" + UPDATED_REDIS_KEY_PREFIX);
    }

    @Test
    @Transactional
    void getAllModulesByRedisKeyPrefixIsInShouldWork() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where redisKeyPrefix in DEFAULT_REDIS_KEY_PREFIX or UPDATED_REDIS_KEY_PREFIX
        defaultModuleShouldBeFound("redisKeyPrefix.in=" + DEFAULT_REDIS_KEY_PREFIX + "," + UPDATED_REDIS_KEY_PREFIX);

        // Get all the moduleList where redisKeyPrefix equals to UPDATED_REDIS_KEY_PREFIX
        defaultModuleShouldNotBeFound("redisKeyPrefix.in=" + UPDATED_REDIS_KEY_PREFIX);
    }

    @Test
    @Transactional
    void getAllModulesByRedisKeyPrefixIsNullOrNotNull() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where redisKeyPrefix is not null
        defaultModuleShouldBeFound("redisKeyPrefix.specified=true");

        // Get all the moduleList where redisKeyPrefix is null
        defaultModuleShouldNotBeFound("redisKeyPrefix.specified=false");
    }

    @Test
    @Transactional
    void getAllModulesByRedisKeyPrefixContainsSomething() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where redisKeyPrefix contains DEFAULT_REDIS_KEY_PREFIX
        defaultModuleShouldBeFound("redisKeyPrefix.contains=" + DEFAULT_REDIS_KEY_PREFIX);

        // Get all the moduleList where redisKeyPrefix contains UPDATED_REDIS_KEY_PREFIX
        defaultModuleShouldNotBeFound("redisKeyPrefix.contains=" + UPDATED_REDIS_KEY_PREFIX);
    }

    @Test
    @Transactional
    void getAllModulesByRedisKeyPrefixNotContainsSomething() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where redisKeyPrefix does not contain DEFAULT_REDIS_KEY_PREFIX
        defaultModuleShouldNotBeFound("redisKeyPrefix.doesNotContain=" + DEFAULT_REDIS_KEY_PREFIX);

        // Get all the moduleList where redisKeyPrefix does not contain UPDATED_REDIS_KEY_PREFIX
        defaultModuleShouldBeFound("redisKeyPrefix.doesNotContain=" + UPDATED_REDIS_KEY_PREFIX);
    }

    @Test
    @Transactional
    void getAllModulesByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where status equals to DEFAULT_STATUS
        defaultModuleShouldBeFound("status.equals=" + DEFAULT_STATUS);

        // Get all the moduleList where status equals to UPDATED_STATUS
        defaultModuleShouldNotBeFound("status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllModulesByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where status in DEFAULT_STATUS or UPDATED_STATUS
        defaultModuleShouldBeFound("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS);

        // Get all the moduleList where status equals to UPDATED_STATUS
        defaultModuleShouldNotBeFound("status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllModulesByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where status is not null
        defaultModuleShouldBeFound("status.specified=true");

        // Get all the moduleList where status is null
        defaultModuleShouldNotBeFound("status.specified=false");
    }

    @Test
    @Transactional
    void getAllModulesByStatusIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where status is greater than or equal to DEFAULT_STATUS
        defaultModuleShouldBeFound("status.greaterThanOrEqual=" + DEFAULT_STATUS);

        // Get all the moduleList where status is greater than or equal to (DEFAULT_STATUS + 1)
        defaultModuleShouldNotBeFound("status.greaterThanOrEqual=" + (DEFAULT_STATUS + 1));
    }

    @Test
    @Transactional
    void getAllModulesByStatusIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where status is less than or equal to DEFAULT_STATUS
        defaultModuleShouldBeFound("status.lessThanOrEqual=" + DEFAULT_STATUS);

        // Get all the moduleList where status is less than or equal to SMALLER_STATUS
        defaultModuleShouldNotBeFound("status.lessThanOrEqual=" + SMALLER_STATUS);
    }

    @Test
    @Transactional
    void getAllModulesByStatusIsLessThanSomething() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where status is less than DEFAULT_STATUS
        defaultModuleShouldNotBeFound("status.lessThan=" + DEFAULT_STATUS);

        // Get all the moduleList where status is less than (DEFAULT_STATUS + 1)
        defaultModuleShouldBeFound("status.lessThan=" + (DEFAULT_STATUS + 1));
    }

    @Test
    @Transactional
    void getAllModulesByStatusIsGreaterThanSomething() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where status is greater than DEFAULT_STATUS
        defaultModuleShouldNotBeFound("status.greaterThan=" + DEFAULT_STATUS);

        // Get all the moduleList where status is greater than SMALLER_STATUS
        defaultModuleShouldBeFound("status.greaterThan=" + SMALLER_STATUS);
    }

    @Test
    @Transactional
    void getAllModulesByLoggingModeIsEqualToSomething() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where loggingMode equals to DEFAULT_LOGGING_MODE
        defaultModuleShouldBeFound("loggingMode.equals=" + DEFAULT_LOGGING_MODE);

        // Get all the moduleList where loggingMode equals to UPDATED_LOGGING_MODE
        defaultModuleShouldNotBeFound("loggingMode.equals=" + UPDATED_LOGGING_MODE);
    }

    @Test
    @Transactional
    void getAllModulesByLoggingModeIsInShouldWork() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where loggingMode in DEFAULT_LOGGING_MODE or UPDATED_LOGGING_MODE
        defaultModuleShouldBeFound("loggingMode.in=" + DEFAULT_LOGGING_MODE + "," + UPDATED_LOGGING_MODE);

        // Get all the moduleList where loggingMode equals to UPDATED_LOGGING_MODE
        defaultModuleShouldNotBeFound("loggingMode.in=" + UPDATED_LOGGING_MODE);
    }

    @Test
    @Transactional
    void getAllModulesByLoggingModeIsNullOrNotNull() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where loggingMode is not null
        defaultModuleShouldBeFound("loggingMode.specified=true");

        // Get all the moduleList where loggingMode is null
        defaultModuleShouldNotBeFound("loggingMode.specified=false");
    }

    @Test
    @Transactional
    void getAllModulesByLoggingModeContainsSomething() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where loggingMode contains DEFAULT_LOGGING_MODE
        defaultModuleShouldBeFound("loggingMode.contains=" + DEFAULT_LOGGING_MODE);

        // Get all the moduleList where loggingMode contains UPDATED_LOGGING_MODE
        defaultModuleShouldNotBeFound("loggingMode.contains=" + UPDATED_LOGGING_MODE);
    }

    @Test
    @Transactional
    void getAllModulesByLoggingModeNotContainsSomething() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where loggingMode does not contain DEFAULT_LOGGING_MODE
        defaultModuleShouldNotBeFound("loggingMode.doesNotContain=" + DEFAULT_LOGGING_MODE);

        // Get all the moduleList where loggingMode does not contain UPDATED_LOGGING_MODE
        defaultModuleShouldBeFound("loggingMode.doesNotContain=" + UPDATED_LOGGING_MODE);
    }

    @Test
    @Transactional
    void getAllModulesByLoggingFilterIsEqualToSomething() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where loggingFilter equals to DEFAULT_LOGGING_FILTER
        defaultModuleShouldBeFound("loggingFilter.equals=" + DEFAULT_LOGGING_FILTER);

        // Get all the moduleList where loggingFilter equals to UPDATED_LOGGING_FILTER
        defaultModuleShouldNotBeFound("loggingFilter.equals=" + UPDATED_LOGGING_FILTER);
    }

    @Test
    @Transactional
    void getAllModulesByLoggingFilterIsInShouldWork() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where loggingFilter in DEFAULT_LOGGING_FILTER or UPDATED_LOGGING_FILTER
        defaultModuleShouldBeFound("loggingFilter.in=" + DEFAULT_LOGGING_FILTER + "," + UPDATED_LOGGING_FILTER);

        // Get all the moduleList where loggingFilter equals to UPDATED_LOGGING_FILTER
        defaultModuleShouldNotBeFound("loggingFilter.in=" + UPDATED_LOGGING_FILTER);
    }

    @Test
    @Transactional
    void getAllModulesByLoggingFilterIsNullOrNotNull() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where loggingFilter is not null
        defaultModuleShouldBeFound("loggingFilter.specified=true");

        // Get all the moduleList where loggingFilter is null
        defaultModuleShouldNotBeFound("loggingFilter.specified=false");
    }

    @Test
    @Transactional
    void getAllModulesByLoggingFilterContainsSomething() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where loggingFilter contains DEFAULT_LOGGING_FILTER
        defaultModuleShouldBeFound("loggingFilter.contains=" + DEFAULT_LOGGING_FILTER);

        // Get all the moduleList where loggingFilter contains UPDATED_LOGGING_FILTER
        defaultModuleShouldNotBeFound("loggingFilter.contains=" + UPDATED_LOGGING_FILTER);
    }

    @Test
    @Transactional
    void getAllModulesByLoggingFilterNotContainsSomething() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where loggingFilter does not contain DEFAULT_LOGGING_FILTER
        defaultModuleShouldNotBeFound("loggingFilter.doesNotContain=" + DEFAULT_LOGGING_FILTER);

        // Get all the moduleList where loggingFilter does not contain UPDATED_LOGGING_FILTER
        defaultModuleShouldBeFound("loggingFilter.doesNotContain=" + UPDATED_LOGGING_FILTER);
    }

    @Test
    @Transactional
    void getAllModulesByDnsNameIsEqualToSomething() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where dnsName equals to DEFAULT_DNS_NAME
        defaultModuleShouldBeFound("dnsName.equals=" + DEFAULT_DNS_NAME);

        // Get all the moduleList where dnsName equals to UPDATED_DNS_NAME
        defaultModuleShouldNotBeFound("dnsName.equals=" + UPDATED_DNS_NAME);
    }

    @Test
    @Transactional
    void getAllModulesByDnsNameIsInShouldWork() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where dnsName in DEFAULT_DNS_NAME or UPDATED_DNS_NAME
        defaultModuleShouldBeFound("dnsName.in=" + DEFAULT_DNS_NAME + "," + UPDATED_DNS_NAME);

        // Get all the moduleList where dnsName equals to UPDATED_DNS_NAME
        defaultModuleShouldNotBeFound("dnsName.in=" + UPDATED_DNS_NAME);
    }

    @Test
    @Transactional
    void getAllModulesByDnsNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where dnsName is not null
        defaultModuleShouldBeFound("dnsName.specified=true");

        // Get all the moduleList where dnsName is null
        defaultModuleShouldNotBeFound("dnsName.specified=false");
    }

    @Test
    @Transactional
    void getAllModulesByDnsNameContainsSomething() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where dnsName contains DEFAULT_DNS_NAME
        defaultModuleShouldBeFound("dnsName.contains=" + DEFAULT_DNS_NAME);

        // Get all the moduleList where dnsName contains UPDATED_DNS_NAME
        defaultModuleShouldNotBeFound("dnsName.contains=" + UPDATED_DNS_NAME);
    }

    @Test
    @Transactional
    void getAllModulesByDnsNameNotContainsSomething() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        // Get all the moduleList where dnsName does not contain DEFAULT_DNS_NAME
        defaultModuleShouldNotBeFound("dnsName.doesNotContain=" + DEFAULT_DNS_NAME);

        // Get all the moduleList where dnsName does not contain UPDATED_DNS_NAME
        defaultModuleShouldBeFound("dnsName.doesNotContain=" + UPDATED_DNS_NAME);
    }

    @Test
    @Transactional
    void getAllModulesByConfigsIsEqualToSomething() throws Exception {
        ConfigEntity configs;
        if (TestUtil.findAll(em, ConfigEntity.class).isEmpty()) {
            moduleRepository.saveAndFlush(moduleEntity);
            configs = ConfigResourceIT.createEntity(em);
        } else {
            configs = TestUtil.findAll(em, ConfigEntity.class).get(0);
        }
        em.persist(configs);
        em.flush();
        moduleEntity.addConfigs(configs);
        moduleRepository.saveAndFlush(moduleEntity);
        Long configsId = configs.getId();

        // Get all the moduleList where configs equals to configsId
        defaultModuleShouldBeFound("configsId.equals=" + configsId);

        // Get all the moduleList where configs equals to (configsId + 1)
        defaultModuleShouldNotBeFound("configsId.equals=" + (configsId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultModuleShouldBeFound(String filter) throws Exception {
        restModuleMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(moduleEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].moduleName").value(hasItem(DEFAULT_MODULE_NAME)))
            .andExpect(jsonPath("$.[*].defaultPort").value(hasItem(DEFAULT_DEFAULT_PORT)))
            .andExpect(jsonPath("$.[*].redisKeyPrefix").value(hasItem(DEFAULT_REDIS_KEY_PREFIX)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.intValue())))
            .andExpect(jsonPath("$.[*].loggingMode").value(hasItem(DEFAULT_LOGGING_MODE)))
            .andExpect(jsonPath("$.[*].loggingFilter").value(hasItem(DEFAULT_LOGGING_FILTER)))
            .andExpect(jsonPath("$.[*].dnsName").value(hasItem(DEFAULT_DNS_NAME)));

        // Check, that the count call also returns 1
        restModuleMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultModuleShouldNotBeFound(String filter) throws Exception {
        restModuleMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restModuleMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingModule() throws Exception {
        // Get the module
        restModuleMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingModule() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        int databaseSizeBeforeUpdate = moduleRepository.findAll().size();

        // Update the module
        ModuleEntity updatedModuleEntity = moduleRepository.findById(moduleEntity.getId()).get();
        // Disconnect from session so that the updates on updatedModuleEntity are not directly saved in db
        em.detach(updatedModuleEntity);
        updatedModuleEntity
            .moduleName(UPDATED_MODULE_NAME)
            .defaultPort(UPDATED_DEFAULT_PORT)
            .redisKeyPrefix(UPDATED_REDIS_KEY_PREFIX)
            .status(UPDATED_STATUS)
            .loggingMode(UPDATED_LOGGING_MODE)
            .loggingFilter(UPDATED_LOGGING_FILTER)
            .dnsName(UPDATED_DNS_NAME);
        ModuleDTO moduleDTO = moduleMapper.toDto(updatedModuleEntity);

        restModuleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, moduleDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(moduleDTO))
            )
            .andExpect(status().isOk());

        // Validate the Module in the database
        List<ModuleEntity> moduleList = moduleRepository.findAll();
        assertThat(moduleList).hasSize(databaseSizeBeforeUpdate);
        ModuleEntity testModule = moduleList.get(moduleList.size() - 1);
        assertThat(testModule.getModuleName()).isEqualTo(UPDATED_MODULE_NAME);
        assertThat(testModule.getDefaultPort()).isEqualTo(UPDATED_DEFAULT_PORT);
        assertThat(testModule.getRedisKeyPrefix()).isEqualTo(UPDATED_REDIS_KEY_PREFIX);
        assertThat(testModule.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testModule.getLoggingMode()).isEqualTo(UPDATED_LOGGING_MODE);
        assertThat(testModule.getLoggingFilter()).isEqualTo(UPDATED_LOGGING_FILTER);
        assertThat(testModule.getDnsName()).isEqualTo(UPDATED_DNS_NAME);
    }

    @Test
    @Transactional
    void putNonExistingModule() throws Exception {
        int databaseSizeBeforeUpdate = moduleRepository.findAll().size();
        moduleEntity.setId(count.incrementAndGet());

        // Create the Module
        ModuleDTO moduleDTO = moduleMapper.toDto(moduleEntity);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restModuleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, moduleDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(moduleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Module in the database
        List<ModuleEntity> moduleList = moduleRepository.findAll();
        assertThat(moduleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchModule() throws Exception {
        int databaseSizeBeforeUpdate = moduleRepository.findAll().size();
        moduleEntity.setId(count.incrementAndGet());

        // Create the Module
        ModuleDTO moduleDTO = moduleMapper.toDto(moduleEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restModuleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(moduleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Module in the database
        List<ModuleEntity> moduleList = moduleRepository.findAll();
        assertThat(moduleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamModule() throws Exception {
        int databaseSizeBeforeUpdate = moduleRepository.findAll().size();
        moduleEntity.setId(count.incrementAndGet());

        // Create the Module
        ModuleDTO moduleDTO = moduleMapper.toDto(moduleEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restModuleMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(moduleDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Module in the database
        List<ModuleEntity> moduleList = moduleRepository.findAll();
        assertThat(moduleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateModuleWithPatch() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        int databaseSizeBeforeUpdate = moduleRepository.findAll().size();

        // Update the module using partial update
        ModuleEntity partialUpdatedModuleEntity = new ModuleEntity();
        partialUpdatedModuleEntity.setId(moduleEntity.getId());

        partialUpdatedModuleEntity.moduleName(UPDATED_MODULE_NAME).defaultPort(UPDATED_DEFAULT_PORT).loggingMode(UPDATED_LOGGING_MODE);

        restModuleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedModuleEntity.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedModuleEntity))
            )
            .andExpect(status().isOk());

        // Validate the Module in the database
        List<ModuleEntity> moduleList = moduleRepository.findAll();
        assertThat(moduleList).hasSize(databaseSizeBeforeUpdate);
        ModuleEntity testModule = moduleList.get(moduleList.size() - 1);
        assertThat(testModule.getModuleName()).isEqualTo(UPDATED_MODULE_NAME);
        assertThat(testModule.getDefaultPort()).isEqualTo(UPDATED_DEFAULT_PORT);
        assertThat(testModule.getRedisKeyPrefix()).isEqualTo(DEFAULT_REDIS_KEY_PREFIX);
        assertThat(testModule.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testModule.getLoggingMode()).isEqualTo(UPDATED_LOGGING_MODE);
        assertThat(testModule.getLoggingFilter()).isEqualTo(DEFAULT_LOGGING_FILTER);
        assertThat(testModule.getDnsName()).isEqualTo(DEFAULT_DNS_NAME);
    }

    @Test
    @Transactional
    void fullUpdateModuleWithPatch() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        int databaseSizeBeforeUpdate = moduleRepository.findAll().size();

        // Update the module using partial update
        ModuleEntity partialUpdatedModuleEntity = new ModuleEntity();
        partialUpdatedModuleEntity.setId(moduleEntity.getId());

        partialUpdatedModuleEntity
            .moduleName(UPDATED_MODULE_NAME)
            .defaultPort(UPDATED_DEFAULT_PORT)
            .redisKeyPrefix(UPDATED_REDIS_KEY_PREFIX)
            .status(UPDATED_STATUS)
            .loggingMode(UPDATED_LOGGING_MODE)
            .loggingFilter(UPDATED_LOGGING_FILTER)
            .dnsName(UPDATED_DNS_NAME);

        restModuleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedModuleEntity.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedModuleEntity))
            )
            .andExpect(status().isOk());

        // Validate the Module in the database
        List<ModuleEntity> moduleList = moduleRepository.findAll();
        assertThat(moduleList).hasSize(databaseSizeBeforeUpdate);
        ModuleEntity testModule = moduleList.get(moduleList.size() - 1);
        assertThat(testModule.getModuleName()).isEqualTo(UPDATED_MODULE_NAME);
        assertThat(testModule.getDefaultPort()).isEqualTo(UPDATED_DEFAULT_PORT);
        assertThat(testModule.getRedisKeyPrefix()).isEqualTo(UPDATED_REDIS_KEY_PREFIX);
        assertThat(testModule.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testModule.getLoggingMode()).isEqualTo(UPDATED_LOGGING_MODE);
        assertThat(testModule.getLoggingFilter()).isEqualTo(UPDATED_LOGGING_FILTER);
        assertThat(testModule.getDnsName()).isEqualTo(UPDATED_DNS_NAME);
    }

    @Test
    @Transactional
    void patchNonExistingModule() throws Exception {
        int databaseSizeBeforeUpdate = moduleRepository.findAll().size();
        moduleEntity.setId(count.incrementAndGet());

        // Create the Module
        ModuleDTO moduleDTO = moduleMapper.toDto(moduleEntity);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restModuleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, moduleDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(moduleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Module in the database
        List<ModuleEntity> moduleList = moduleRepository.findAll();
        assertThat(moduleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchModule() throws Exception {
        int databaseSizeBeforeUpdate = moduleRepository.findAll().size();
        moduleEntity.setId(count.incrementAndGet());

        // Create the Module
        ModuleDTO moduleDTO = moduleMapper.toDto(moduleEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restModuleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(moduleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Module in the database
        List<ModuleEntity> moduleList = moduleRepository.findAll();
        assertThat(moduleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamModule() throws Exception {
        int databaseSizeBeforeUpdate = moduleRepository.findAll().size();
        moduleEntity.setId(count.incrementAndGet());

        // Create the Module
        ModuleDTO moduleDTO = moduleMapper.toDto(moduleEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restModuleMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(moduleDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Module in the database
        List<ModuleEntity> moduleList = moduleRepository.findAll();
        assertThat(moduleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteModule() throws Exception {
        // Initialize the database
        moduleRepository.saveAndFlush(moduleEntity);

        int databaseSizeBeforeDelete = moduleRepository.findAll().size();

        // Delete the module
        restModuleMockMvc
            .perform(delete(ENTITY_API_URL_ID, moduleEntity.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<ModuleEntity> moduleList = moduleRepository.findAll();
        assertThat(moduleList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
