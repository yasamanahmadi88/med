package com.behsa.medportal.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.behsa.medportal.IntegrationTest;
import com.behsa.medportal.domain.InstanceEntity;
import com.behsa.medportal.repository.InstanceRepository;
import com.behsa.medportal.service.dto.InstanceDTO;
import com.behsa.medportal.service.mapper.InstanceMapper;
import java.time.LocalDate;
import java.time.ZoneId;
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
 * Integration tests for the {@link InstanceResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class InstanceResourceIT {

    private static final String DEFAULT_MODULE_NAME = "AAAAAAAAAA";
    private static final String UPDATED_MODULE_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_PORT = "AAAAAAAAAA";
    private static final String UPDATED_PORT = "BBBBBBBBBB";

    private static final String DEFAULT_MODULE_STATUS = "AAAAAAAAAA";
    private static final String UPDATED_MODULE_STATUS = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_LAST_UPDATE_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_LAST_UPDATE_DATE = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_LAST_UPDATE_DATE = LocalDate.ofEpochDay(-1L);

    private static final Long DEFAULT_THREAD_POOL_QUEUE_SIZE = 1L;
    private static final Long UPDATED_THREAD_POOL_QUEUE_SIZE = 2L;
    private static final Long SMALLER_THREAD_POOL_QUEUE_SIZE = 1L - 1L;

    private static final Long DEFAULT_MODULE_START_TIME = 1L;
    private static final Long UPDATED_MODULE_START_TIME = 2L;
    private static final Long SMALLER_MODULE_START_TIME = 1L - 1L;

    private static final Long DEFAULT_TOTAL_PROCESSED_WORK = 1L;
    private static final Long UPDATED_TOTAL_PROCESSED_WORK = 2L;
    private static final Long SMALLER_TOTAL_PROCESSED_WORK = 1L - 1L;

    private static final Long DEFAULT_TOTAL_PROCESSED_TASK = 1L;
    private static final Long UPDATED_TOTAL_PROCESSED_TASK = 2L;
    private static final Long SMALLER_TOTAL_PROCESSED_TASK = 1L - 1L;

    private static final String DEFAULT_PROCESSED_STATISTICS = "AAAAAAAAAA";
    private static final String UPDATED_PROCESSED_STATISTICS = "BBBBBBBBBB";

    private static final String DEFAULT_HOST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_HOST_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/instances";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private InstanceRepository instanceRepository;

    @Autowired
    private InstanceMapper instanceMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restInstanceMockMvc;

    private InstanceEntity instanceEntity;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static InstanceEntity createEntity(EntityManager em) {
        InstanceEntity instanceEntity = new InstanceEntity()
            .moduleName(DEFAULT_MODULE_NAME)
            .port(DEFAULT_PORT)
            .moduleStatus(DEFAULT_MODULE_STATUS)
            .lastUpdateDate(DEFAULT_LAST_UPDATE_DATE)
            .threadPoolQueueSize(DEFAULT_THREAD_POOL_QUEUE_SIZE)
            .moduleStartTime(DEFAULT_MODULE_START_TIME)
            .totalProcessedWork(DEFAULT_TOTAL_PROCESSED_WORK)
            .totalProcessedTask(DEFAULT_TOTAL_PROCESSED_TASK)
            .processedStatistics(DEFAULT_PROCESSED_STATISTICS)
            .hostName(DEFAULT_HOST_NAME);
        return instanceEntity;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static InstanceEntity createUpdatedEntity(EntityManager em) {
        InstanceEntity instanceEntity = new InstanceEntity()
            .moduleName(UPDATED_MODULE_NAME)
            .port(UPDATED_PORT)
            .moduleStatus(UPDATED_MODULE_STATUS)
            .lastUpdateDate(UPDATED_LAST_UPDATE_DATE)
            .threadPoolQueueSize(UPDATED_THREAD_POOL_QUEUE_SIZE)
            .moduleStartTime(UPDATED_MODULE_START_TIME)
            .totalProcessedWork(UPDATED_TOTAL_PROCESSED_WORK)
            .totalProcessedTask(UPDATED_TOTAL_PROCESSED_TASK)
            .processedStatistics(UPDATED_PROCESSED_STATISTICS)
            .hostName(UPDATED_HOST_NAME);
        return instanceEntity;
    }

    @BeforeEach
    public void initTest() {
        instanceEntity = createEntity(em);
    }

    @Test
    @Transactional
    void createInstance() throws Exception {
        int databaseSizeBeforeCreate = instanceRepository.findAll().size();
        // Create the Instance
        InstanceDTO instanceDTO = instanceMapper.toDto(instanceEntity);
        restInstanceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(instanceDTO)))
            .andExpect(status().isCreated());

        // Validate the Instance in the database
        List<InstanceEntity> instanceList = instanceRepository.findAll();
        assertThat(instanceList).hasSize(databaseSizeBeforeCreate + 1);
        InstanceEntity testInstance = instanceList.get(instanceList.size() - 1);
        assertThat(testInstance.getModuleName()).isEqualTo(DEFAULT_MODULE_NAME);
        assertThat(testInstance.getPort()).isEqualTo(DEFAULT_PORT);
        assertThat(testInstance.getModuleStatus()).isEqualTo(DEFAULT_MODULE_STATUS);
        assertThat(testInstance.getLastUpdateDate()).isEqualTo(DEFAULT_LAST_UPDATE_DATE);
        assertThat(testInstance.getThreadPoolQueueSize()).isEqualTo(DEFAULT_THREAD_POOL_QUEUE_SIZE);
        assertThat(testInstance.getModuleStartTime()).isEqualTo(DEFAULT_MODULE_START_TIME);
        assertThat(testInstance.getTotalProcessedWork()).isEqualTo(DEFAULT_TOTAL_PROCESSED_WORK);
        assertThat(testInstance.getTotalProcessedTask()).isEqualTo(DEFAULT_TOTAL_PROCESSED_TASK);
        assertThat(testInstance.getProcessedStatistics()).isEqualTo(DEFAULT_PROCESSED_STATISTICS);
        assertThat(testInstance.getHostName()).isEqualTo(DEFAULT_HOST_NAME);
    }

    @Test
    @Transactional
    void createInstanceWithExistingId() throws Exception {
        // Create the Instance with an existing ID
        instanceEntity.setId(1L);
        InstanceDTO instanceDTO = instanceMapper.toDto(instanceEntity);

        int databaseSizeBeforeCreate = instanceRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restInstanceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(instanceDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Instance in the database
        List<InstanceEntity> instanceList = instanceRepository.findAll();
        assertThat(instanceList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkModuleNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = instanceRepository.findAll().size();
        // set the field null
        instanceEntity.setModuleName(null);

        // Create the Instance, which fails.
        InstanceDTO instanceDTO = instanceMapper.toDto(instanceEntity);

        restInstanceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(instanceDTO)))
            .andExpect(status().isBadRequest());

        List<InstanceEntity> instanceList = instanceRepository.findAll();
        assertThat(instanceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPortIsRequired() throws Exception {
        int databaseSizeBeforeTest = instanceRepository.findAll().size();
        // set the field null
        instanceEntity.setPort(null);

        // Create the Instance, which fails.
        InstanceDTO instanceDTO = instanceMapper.toDto(instanceEntity);

        restInstanceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(instanceDTO)))
            .andExpect(status().isBadRequest());

        List<InstanceEntity> instanceList = instanceRepository.findAll();
        assertThat(instanceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkModuleStatusIsRequired() throws Exception {
        int databaseSizeBeforeTest = instanceRepository.findAll().size();
        // set the field null
        instanceEntity.setModuleStatus(null);

        // Create the Instance, which fails.
        InstanceDTO instanceDTO = instanceMapper.toDto(instanceEntity);

        restInstanceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(instanceDTO)))
            .andExpect(status().isBadRequest());

        List<InstanceEntity> instanceList = instanceRepository.findAll();
        assertThat(instanceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkLastUpdateDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = instanceRepository.findAll().size();
        // set the field null
        instanceEntity.setLastUpdateDate(null);

        // Create the Instance, which fails.
        InstanceDTO instanceDTO = instanceMapper.toDto(instanceEntity);

        restInstanceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(instanceDTO)))
            .andExpect(status().isBadRequest());

        List<InstanceEntity> instanceList = instanceRepository.findAll();
        assertThat(instanceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkHostNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = instanceRepository.findAll().size();
        // set the field null
        instanceEntity.setHostName(null);

        // Create the Instance, which fails.
        InstanceDTO instanceDTO = instanceMapper.toDto(instanceEntity);

        restInstanceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(instanceDTO)))
            .andExpect(status().isBadRequest());

        List<InstanceEntity> instanceList = instanceRepository.findAll();
        assertThat(instanceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllInstances() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList
        restInstanceMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(instanceEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].moduleName").value(hasItem(DEFAULT_MODULE_NAME)))
            .andExpect(jsonPath("$.[*].port").value(hasItem(DEFAULT_PORT)))
            .andExpect(jsonPath("$.[*].moduleStatus").value(hasItem(DEFAULT_MODULE_STATUS)))
            .andExpect(jsonPath("$.[*].lastUpdateDate").value(hasItem(DEFAULT_LAST_UPDATE_DATE.toString())))
            .andExpect(jsonPath("$.[*].threadPoolQueueSize").value(hasItem(DEFAULT_THREAD_POOL_QUEUE_SIZE.intValue())))
            .andExpect(jsonPath("$.[*].moduleStartTime").value(hasItem(DEFAULT_MODULE_START_TIME.intValue())))
            .andExpect(jsonPath("$.[*].totalProcessedWork").value(hasItem(DEFAULT_TOTAL_PROCESSED_WORK.intValue())))
            .andExpect(jsonPath("$.[*].totalProcessedTask").value(hasItem(DEFAULT_TOTAL_PROCESSED_TASK.intValue())))
            .andExpect(jsonPath("$.[*].processedStatistics").value(hasItem(DEFAULT_PROCESSED_STATISTICS)))
            .andExpect(jsonPath("$.[*].hostName").value(hasItem(DEFAULT_HOST_NAME)));
    }

    @Test
    @Transactional
    void getInstance() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get the instance
        restInstanceMockMvc
            .perform(get(ENTITY_API_URL_ID, instanceEntity.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(instanceEntity.getId().intValue()))
            .andExpect(jsonPath("$.moduleName").value(DEFAULT_MODULE_NAME))
            .andExpect(jsonPath("$.port").value(DEFAULT_PORT))
            .andExpect(jsonPath("$.moduleStatus").value(DEFAULT_MODULE_STATUS))
            .andExpect(jsonPath("$.lastUpdateDate").value(DEFAULT_LAST_UPDATE_DATE.toString()))
            .andExpect(jsonPath("$.threadPoolQueueSize").value(DEFAULT_THREAD_POOL_QUEUE_SIZE.intValue()))
            .andExpect(jsonPath("$.moduleStartTime").value(DEFAULT_MODULE_START_TIME.intValue()))
            .andExpect(jsonPath("$.totalProcessedWork").value(DEFAULT_TOTAL_PROCESSED_WORK.intValue()))
            .andExpect(jsonPath("$.totalProcessedTask").value(DEFAULT_TOTAL_PROCESSED_TASK.intValue()))
            .andExpect(jsonPath("$.processedStatistics").value(DEFAULT_PROCESSED_STATISTICS))
            .andExpect(jsonPath("$.hostName").value(DEFAULT_HOST_NAME));
    }

    @Test
    @Transactional
    void getInstancesByIdFiltering() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        Long id = instanceEntity.getId();

        defaultInstanceShouldBeFound("id.equals=" + id);
        defaultInstanceShouldNotBeFound("id.notEquals=" + id);

        defaultInstanceShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultInstanceShouldNotBeFound("id.greaterThan=" + id);

        defaultInstanceShouldBeFound("id.lessThanOrEqual=" + id);
        defaultInstanceShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllInstancesByModuleNameIsEqualToSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where moduleName equals to DEFAULT_MODULE_NAME
        defaultInstanceShouldBeFound("moduleName.equals=" + DEFAULT_MODULE_NAME);

        // Get all the instanceList where moduleName equals to UPDATED_MODULE_NAME
        defaultInstanceShouldNotBeFound("moduleName.equals=" + UPDATED_MODULE_NAME);
    }

    @Test
    @Transactional
    void getAllInstancesByModuleNameIsInShouldWork() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where moduleName in DEFAULT_MODULE_NAME or UPDATED_MODULE_NAME
        defaultInstanceShouldBeFound("moduleName.in=" + DEFAULT_MODULE_NAME + "," + UPDATED_MODULE_NAME);

        // Get all the instanceList where moduleName equals to UPDATED_MODULE_NAME
        defaultInstanceShouldNotBeFound("moduleName.in=" + UPDATED_MODULE_NAME);
    }

    @Test
    @Transactional
    void getAllInstancesByModuleNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where moduleName is not null
        defaultInstanceShouldBeFound("moduleName.specified=true");

        // Get all the instanceList where moduleName is null
        defaultInstanceShouldNotBeFound("moduleName.specified=false");
    }

    @Test
    @Transactional
    void getAllInstancesByModuleNameContainsSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where moduleName contains DEFAULT_MODULE_NAME
        defaultInstanceShouldBeFound("moduleName.contains=" + DEFAULT_MODULE_NAME);

        // Get all the instanceList where moduleName contains UPDATED_MODULE_NAME
        defaultInstanceShouldNotBeFound("moduleName.contains=" + UPDATED_MODULE_NAME);
    }

    @Test
    @Transactional
    void getAllInstancesByModuleNameNotContainsSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where moduleName does not contain DEFAULT_MODULE_NAME
        defaultInstanceShouldNotBeFound("moduleName.doesNotContain=" + DEFAULT_MODULE_NAME);

        // Get all the instanceList where moduleName does not contain UPDATED_MODULE_NAME
        defaultInstanceShouldBeFound("moduleName.doesNotContain=" + UPDATED_MODULE_NAME);
    }

    @Test
    @Transactional
    void getAllInstancesByPortIsEqualToSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where port equals to DEFAULT_PORT
        defaultInstanceShouldBeFound("port.equals=" + DEFAULT_PORT);

        // Get all the instanceList where port equals to UPDATED_PORT
        defaultInstanceShouldNotBeFound("port.equals=" + UPDATED_PORT);
    }

    @Test
    @Transactional
    void getAllInstancesByPortIsInShouldWork() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where port in DEFAULT_PORT or UPDATED_PORT
        defaultInstanceShouldBeFound("port.in=" + DEFAULT_PORT + "," + UPDATED_PORT);

        // Get all the instanceList where port equals to UPDATED_PORT
        defaultInstanceShouldNotBeFound("port.in=" + UPDATED_PORT);
    }

    @Test
    @Transactional
    void getAllInstancesByPortIsNullOrNotNull() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where port is not null
        defaultInstanceShouldBeFound("port.specified=true");

        // Get all the instanceList where port is null
        defaultInstanceShouldNotBeFound("port.specified=false");
    }

    @Test
    @Transactional
    void getAllInstancesByPortContainsSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where port contains DEFAULT_PORT
        defaultInstanceShouldBeFound("port.contains=" + DEFAULT_PORT);

        // Get all the instanceList where port contains UPDATED_PORT
        defaultInstanceShouldNotBeFound("port.contains=" + UPDATED_PORT);
    }

    @Test
    @Transactional
    void getAllInstancesByPortNotContainsSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where port does not contain DEFAULT_PORT
        defaultInstanceShouldNotBeFound("port.doesNotContain=" + DEFAULT_PORT);

        // Get all the instanceList where port does not contain UPDATED_PORT
        defaultInstanceShouldBeFound("port.doesNotContain=" + UPDATED_PORT);
    }

    @Test
    @Transactional
    void getAllInstancesByModuleStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where moduleStatus equals to DEFAULT_MODULE_STATUS
        defaultInstanceShouldBeFound("moduleStatus.equals=" + DEFAULT_MODULE_STATUS);

        // Get all the instanceList where moduleStatus equals to UPDATED_MODULE_STATUS
        defaultInstanceShouldNotBeFound("moduleStatus.equals=" + UPDATED_MODULE_STATUS);
    }

    @Test
    @Transactional
    void getAllInstancesByModuleStatusIsInShouldWork() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where moduleStatus in DEFAULT_MODULE_STATUS or UPDATED_MODULE_STATUS
        defaultInstanceShouldBeFound("moduleStatus.in=" + DEFAULT_MODULE_STATUS + "," + UPDATED_MODULE_STATUS);

        // Get all the instanceList where moduleStatus equals to UPDATED_MODULE_STATUS
        defaultInstanceShouldNotBeFound("moduleStatus.in=" + UPDATED_MODULE_STATUS);
    }

    @Test
    @Transactional
    void getAllInstancesByModuleStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where moduleStatus is not null
        defaultInstanceShouldBeFound("moduleStatus.specified=true");

        // Get all the instanceList where moduleStatus is null
        defaultInstanceShouldNotBeFound("moduleStatus.specified=false");
    }

    @Test
    @Transactional
    void getAllInstancesByModuleStatusContainsSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where moduleStatus contains DEFAULT_MODULE_STATUS
        defaultInstanceShouldBeFound("moduleStatus.contains=" + DEFAULT_MODULE_STATUS);

        // Get all the instanceList where moduleStatus contains UPDATED_MODULE_STATUS
        defaultInstanceShouldNotBeFound("moduleStatus.contains=" + UPDATED_MODULE_STATUS);
    }

    @Test
    @Transactional
    void getAllInstancesByModuleStatusNotContainsSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where moduleStatus does not contain DEFAULT_MODULE_STATUS
        defaultInstanceShouldNotBeFound("moduleStatus.doesNotContain=" + DEFAULT_MODULE_STATUS);

        // Get all the instanceList where moduleStatus does not contain UPDATED_MODULE_STATUS
        defaultInstanceShouldBeFound("moduleStatus.doesNotContain=" + UPDATED_MODULE_STATUS);
    }

    @Test
    @Transactional
    void getAllInstancesByLastUpdateDateIsEqualToSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where lastUpdateDate equals to DEFAULT_LAST_UPDATE_DATE
        defaultInstanceShouldBeFound("lastUpdateDate.equals=" + DEFAULT_LAST_UPDATE_DATE);

        // Get all the instanceList where lastUpdateDate equals to UPDATED_LAST_UPDATE_DATE
        defaultInstanceShouldNotBeFound("lastUpdateDate.equals=" + UPDATED_LAST_UPDATE_DATE);
    }

    @Test
    @Transactional
    void getAllInstancesByLastUpdateDateIsInShouldWork() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where lastUpdateDate in DEFAULT_LAST_UPDATE_DATE or UPDATED_LAST_UPDATE_DATE
        defaultInstanceShouldBeFound("lastUpdateDate.in=" + DEFAULT_LAST_UPDATE_DATE + "," + UPDATED_LAST_UPDATE_DATE);

        // Get all the instanceList where lastUpdateDate equals to UPDATED_LAST_UPDATE_DATE
        defaultInstanceShouldNotBeFound("lastUpdateDate.in=" + UPDATED_LAST_UPDATE_DATE);
    }

    @Test
    @Transactional
    void getAllInstancesByLastUpdateDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where lastUpdateDate is not null
        defaultInstanceShouldBeFound("lastUpdateDate.specified=true");

        // Get all the instanceList where lastUpdateDate is null
        defaultInstanceShouldNotBeFound("lastUpdateDate.specified=false");
    }

    @Test
    @Transactional
    void getAllInstancesByLastUpdateDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where lastUpdateDate is greater than or equal to DEFAULT_LAST_UPDATE_DATE
        defaultInstanceShouldBeFound("lastUpdateDate.greaterThanOrEqual=" + DEFAULT_LAST_UPDATE_DATE);

        // Get all the instanceList where lastUpdateDate is greater than or equal to UPDATED_LAST_UPDATE_DATE
        defaultInstanceShouldNotBeFound("lastUpdateDate.greaterThanOrEqual=" + UPDATED_LAST_UPDATE_DATE);
    }

    @Test
    @Transactional
    void getAllInstancesByLastUpdateDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where lastUpdateDate is less than or equal to DEFAULT_LAST_UPDATE_DATE
        defaultInstanceShouldBeFound("lastUpdateDate.lessThanOrEqual=" + DEFAULT_LAST_UPDATE_DATE);

        // Get all the instanceList where lastUpdateDate is less than or equal to SMALLER_LAST_UPDATE_DATE
        defaultInstanceShouldNotBeFound("lastUpdateDate.lessThanOrEqual=" + SMALLER_LAST_UPDATE_DATE);
    }

    @Test
    @Transactional
    void getAllInstancesByLastUpdateDateIsLessThanSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where lastUpdateDate is less than DEFAULT_LAST_UPDATE_DATE
        defaultInstanceShouldNotBeFound("lastUpdateDate.lessThan=" + DEFAULT_LAST_UPDATE_DATE);

        // Get all the instanceList where lastUpdateDate is less than UPDATED_LAST_UPDATE_DATE
        defaultInstanceShouldBeFound("lastUpdateDate.lessThan=" + UPDATED_LAST_UPDATE_DATE);
    }

    @Test
    @Transactional
    void getAllInstancesByLastUpdateDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where lastUpdateDate is greater than DEFAULT_LAST_UPDATE_DATE
        defaultInstanceShouldNotBeFound("lastUpdateDate.greaterThan=" + DEFAULT_LAST_UPDATE_DATE);

        // Get all the instanceList where lastUpdateDate is greater than SMALLER_LAST_UPDATE_DATE
        defaultInstanceShouldBeFound("lastUpdateDate.greaterThan=" + SMALLER_LAST_UPDATE_DATE);
    }

    @Test
    @Transactional
    void getAllInstancesByThreadPoolQueueSizeIsEqualToSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where threadPoolQueueSize equals to DEFAULT_THREAD_POOL_QUEUE_SIZE
        defaultInstanceShouldBeFound("threadPoolQueueSize.equals=" + DEFAULT_THREAD_POOL_QUEUE_SIZE);

        // Get all the instanceList where threadPoolQueueSize equals to UPDATED_THREAD_POOL_QUEUE_SIZE
        defaultInstanceShouldNotBeFound("threadPoolQueueSize.equals=" + UPDATED_THREAD_POOL_QUEUE_SIZE);
    }

    @Test
    @Transactional
    void getAllInstancesByThreadPoolQueueSizeIsInShouldWork() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where threadPoolQueueSize in DEFAULT_THREAD_POOL_QUEUE_SIZE or UPDATED_THREAD_POOL_QUEUE_SIZE
        defaultInstanceShouldBeFound("threadPoolQueueSize.in=" + DEFAULT_THREAD_POOL_QUEUE_SIZE + "," + UPDATED_THREAD_POOL_QUEUE_SIZE);

        // Get all the instanceList where threadPoolQueueSize equals to UPDATED_THREAD_POOL_QUEUE_SIZE
        defaultInstanceShouldNotBeFound("threadPoolQueueSize.in=" + UPDATED_THREAD_POOL_QUEUE_SIZE);
    }

    @Test
    @Transactional
    void getAllInstancesByThreadPoolQueueSizeIsNullOrNotNull() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where threadPoolQueueSize is not null
        defaultInstanceShouldBeFound("threadPoolQueueSize.specified=true");

        // Get all the instanceList where threadPoolQueueSize is null
        defaultInstanceShouldNotBeFound("threadPoolQueueSize.specified=false");
    }

    @Test
    @Transactional
    void getAllInstancesByThreadPoolQueueSizeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where threadPoolQueueSize is greater than or equal to DEFAULT_THREAD_POOL_QUEUE_SIZE
        defaultInstanceShouldBeFound("threadPoolQueueSize.greaterThanOrEqual=" + DEFAULT_THREAD_POOL_QUEUE_SIZE);

        // Get all the instanceList where threadPoolQueueSize is greater than or equal to UPDATED_THREAD_POOL_QUEUE_SIZE
        defaultInstanceShouldNotBeFound("threadPoolQueueSize.greaterThanOrEqual=" + UPDATED_THREAD_POOL_QUEUE_SIZE);
    }

    @Test
    @Transactional
    void getAllInstancesByThreadPoolQueueSizeIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where threadPoolQueueSize is less than or equal to DEFAULT_THREAD_POOL_QUEUE_SIZE
        defaultInstanceShouldBeFound("threadPoolQueueSize.lessThanOrEqual=" + DEFAULT_THREAD_POOL_QUEUE_SIZE);

        // Get all the instanceList where threadPoolQueueSize is less than or equal to SMALLER_THREAD_POOL_QUEUE_SIZE
        defaultInstanceShouldNotBeFound("threadPoolQueueSize.lessThanOrEqual=" + SMALLER_THREAD_POOL_QUEUE_SIZE);
    }

    @Test
    @Transactional
    void getAllInstancesByThreadPoolQueueSizeIsLessThanSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where threadPoolQueueSize is less than DEFAULT_THREAD_POOL_QUEUE_SIZE
        defaultInstanceShouldNotBeFound("threadPoolQueueSize.lessThan=" + DEFAULT_THREAD_POOL_QUEUE_SIZE);

        // Get all the instanceList where threadPoolQueueSize is less than UPDATED_THREAD_POOL_QUEUE_SIZE
        defaultInstanceShouldBeFound("threadPoolQueueSize.lessThan=" + UPDATED_THREAD_POOL_QUEUE_SIZE);
    }

    @Test
    @Transactional
    void getAllInstancesByThreadPoolQueueSizeIsGreaterThanSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where threadPoolQueueSize is greater than DEFAULT_THREAD_POOL_QUEUE_SIZE
        defaultInstanceShouldNotBeFound("threadPoolQueueSize.greaterThan=" + DEFAULT_THREAD_POOL_QUEUE_SIZE);

        // Get all the instanceList where threadPoolQueueSize is greater than SMALLER_THREAD_POOL_QUEUE_SIZE
        defaultInstanceShouldBeFound("threadPoolQueueSize.greaterThan=" + SMALLER_THREAD_POOL_QUEUE_SIZE);
    }

    @Test
    @Transactional
    void getAllInstancesByModuleStartTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where moduleStartTime equals to DEFAULT_MODULE_START_TIME
        defaultInstanceShouldBeFound("moduleStartTime.equals=" + DEFAULT_MODULE_START_TIME);

        // Get all the instanceList where moduleStartTime equals to UPDATED_MODULE_START_TIME
        defaultInstanceShouldNotBeFound("moduleStartTime.equals=" + UPDATED_MODULE_START_TIME);
    }

    @Test
    @Transactional
    void getAllInstancesByModuleStartTimeIsInShouldWork() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where moduleStartTime in DEFAULT_MODULE_START_TIME or UPDATED_MODULE_START_TIME
        defaultInstanceShouldBeFound("moduleStartTime.in=" + DEFAULT_MODULE_START_TIME + "," + UPDATED_MODULE_START_TIME);

        // Get all the instanceList where moduleStartTime equals to UPDATED_MODULE_START_TIME
        defaultInstanceShouldNotBeFound("moduleStartTime.in=" + UPDATED_MODULE_START_TIME);
    }

    @Test
    @Transactional
    void getAllInstancesByModuleStartTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where moduleStartTime is not null
        defaultInstanceShouldBeFound("moduleStartTime.specified=true");

        // Get all the instanceList where moduleStartTime is null
        defaultInstanceShouldNotBeFound("moduleStartTime.specified=false");
    }

    @Test
    @Transactional
    void getAllInstancesByModuleStartTimeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where moduleStartTime is greater than or equal to DEFAULT_MODULE_START_TIME
        defaultInstanceShouldBeFound("moduleStartTime.greaterThanOrEqual=" + DEFAULT_MODULE_START_TIME);

        // Get all the instanceList where moduleStartTime is greater than or equal to UPDATED_MODULE_START_TIME
        defaultInstanceShouldNotBeFound("moduleStartTime.greaterThanOrEqual=" + UPDATED_MODULE_START_TIME);
    }

    @Test
    @Transactional
    void getAllInstancesByModuleStartTimeIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where moduleStartTime is less than or equal to DEFAULT_MODULE_START_TIME
        defaultInstanceShouldBeFound("moduleStartTime.lessThanOrEqual=" + DEFAULT_MODULE_START_TIME);

        // Get all the instanceList where moduleStartTime is less than or equal to SMALLER_MODULE_START_TIME
        defaultInstanceShouldNotBeFound("moduleStartTime.lessThanOrEqual=" + SMALLER_MODULE_START_TIME);
    }

    @Test
    @Transactional
    void getAllInstancesByModuleStartTimeIsLessThanSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where moduleStartTime is less than DEFAULT_MODULE_START_TIME
        defaultInstanceShouldNotBeFound("moduleStartTime.lessThan=" + DEFAULT_MODULE_START_TIME);

        // Get all the instanceList where moduleStartTime is less than UPDATED_MODULE_START_TIME
        defaultInstanceShouldBeFound("moduleStartTime.lessThan=" + UPDATED_MODULE_START_TIME);
    }

    @Test
    @Transactional
    void getAllInstancesByModuleStartTimeIsGreaterThanSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where moduleStartTime is greater than DEFAULT_MODULE_START_TIME
        defaultInstanceShouldNotBeFound("moduleStartTime.greaterThan=" + DEFAULT_MODULE_START_TIME);

        // Get all the instanceList where moduleStartTime is greater than SMALLER_MODULE_START_TIME
        defaultInstanceShouldBeFound("moduleStartTime.greaterThan=" + SMALLER_MODULE_START_TIME);
    }

    @Test
    @Transactional
    void getAllInstancesByTotalProcessedWorkIsEqualToSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where totalProcessedWork equals to DEFAULT_TOTAL_PROCESSED_WORK
        defaultInstanceShouldBeFound("totalProcessedWork.equals=" + DEFAULT_TOTAL_PROCESSED_WORK);

        // Get all the instanceList where totalProcessedWork equals to UPDATED_TOTAL_PROCESSED_WORK
        defaultInstanceShouldNotBeFound("totalProcessedWork.equals=" + UPDATED_TOTAL_PROCESSED_WORK);
    }

    @Test
    @Transactional
    void getAllInstancesByTotalProcessedWorkIsInShouldWork() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where totalProcessedWork in DEFAULT_TOTAL_PROCESSED_WORK or UPDATED_TOTAL_PROCESSED_WORK
        defaultInstanceShouldBeFound("totalProcessedWork.in=" + DEFAULT_TOTAL_PROCESSED_WORK + "," + UPDATED_TOTAL_PROCESSED_WORK);

        // Get all the instanceList where totalProcessedWork equals to UPDATED_TOTAL_PROCESSED_WORK
        defaultInstanceShouldNotBeFound("totalProcessedWork.in=" + UPDATED_TOTAL_PROCESSED_WORK);
    }

    @Test
    @Transactional
    void getAllInstancesByTotalProcessedWorkIsNullOrNotNull() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where totalProcessedWork is not null
        defaultInstanceShouldBeFound("totalProcessedWork.specified=true");

        // Get all the instanceList where totalProcessedWork is null
        defaultInstanceShouldNotBeFound("totalProcessedWork.specified=false");
    }

    @Test
    @Transactional
    void getAllInstancesByTotalProcessedWorkIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where totalProcessedWork is greater than or equal to DEFAULT_TOTAL_PROCESSED_WORK
        defaultInstanceShouldBeFound("totalProcessedWork.greaterThanOrEqual=" + DEFAULT_TOTAL_PROCESSED_WORK);

        // Get all the instanceList where totalProcessedWork is greater than or equal to UPDATED_TOTAL_PROCESSED_WORK
        defaultInstanceShouldNotBeFound("totalProcessedWork.greaterThanOrEqual=" + UPDATED_TOTAL_PROCESSED_WORK);
    }

    @Test
    @Transactional
    void getAllInstancesByTotalProcessedWorkIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where totalProcessedWork is less than or equal to DEFAULT_TOTAL_PROCESSED_WORK
        defaultInstanceShouldBeFound("totalProcessedWork.lessThanOrEqual=" + DEFAULT_TOTAL_PROCESSED_WORK);

        // Get all the instanceList where totalProcessedWork is less than or equal to SMALLER_TOTAL_PROCESSED_WORK
        defaultInstanceShouldNotBeFound("totalProcessedWork.lessThanOrEqual=" + SMALLER_TOTAL_PROCESSED_WORK);
    }

    @Test
    @Transactional
    void getAllInstancesByTotalProcessedWorkIsLessThanSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where totalProcessedWork is less than DEFAULT_TOTAL_PROCESSED_WORK
        defaultInstanceShouldNotBeFound("totalProcessedWork.lessThan=" + DEFAULT_TOTAL_PROCESSED_WORK);

        // Get all the instanceList where totalProcessedWork is less than UPDATED_TOTAL_PROCESSED_WORK
        defaultInstanceShouldBeFound("totalProcessedWork.lessThan=" + UPDATED_TOTAL_PROCESSED_WORK);
    }

    @Test
    @Transactional
    void getAllInstancesByTotalProcessedWorkIsGreaterThanSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where totalProcessedWork is greater than DEFAULT_TOTAL_PROCESSED_WORK
        defaultInstanceShouldNotBeFound("totalProcessedWork.greaterThan=" + DEFAULT_TOTAL_PROCESSED_WORK);

        // Get all the instanceList where totalProcessedWork is greater than SMALLER_TOTAL_PROCESSED_WORK
        defaultInstanceShouldBeFound("totalProcessedWork.greaterThan=" + SMALLER_TOTAL_PROCESSED_WORK);
    }

    @Test
    @Transactional
    void getAllInstancesByTotalProcessedTaskIsEqualToSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where totalProcessedTask equals to DEFAULT_TOTAL_PROCESSED_TASK
        defaultInstanceShouldBeFound("totalProcessedTask.equals=" + DEFAULT_TOTAL_PROCESSED_TASK);

        // Get all the instanceList where totalProcessedTask equals to UPDATED_TOTAL_PROCESSED_TASK
        defaultInstanceShouldNotBeFound("totalProcessedTask.equals=" + UPDATED_TOTAL_PROCESSED_TASK);
    }

    @Test
    @Transactional
    void getAllInstancesByTotalProcessedTaskIsInShouldWork() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where totalProcessedTask in DEFAULT_TOTAL_PROCESSED_TASK or UPDATED_TOTAL_PROCESSED_TASK
        defaultInstanceShouldBeFound("totalProcessedTask.in=" + DEFAULT_TOTAL_PROCESSED_TASK + "," + UPDATED_TOTAL_PROCESSED_TASK);

        // Get all the instanceList where totalProcessedTask equals to UPDATED_TOTAL_PROCESSED_TASK
        defaultInstanceShouldNotBeFound("totalProcessedTask.in=" + UPDATED_TOTAL_PROCESSED_TASK);
    }

    @Test
    @Transactional
    void getAllInstancesByTotalProcessedTaskIsNullOrNotNull() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where totalProcessedTask is not null
        defaultInstanceShouldBeFound("totalProcessedTask.specified=true");

        // Get all the instanceList where totalProcessedTask is null
        defaultInstanceShouldNotBeFound("totalProcessedTask.specified=false");
    }

    @Test
    @Transactional
    void getAllInstancesByTotalProcessedTaskIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where totalProcessedTask is greater than or equal to DEFAULT_TOTAL_PROCESSED_TASK
        defaultInstanceShouldBeFound("totalProcessedTask.greaterThanOrEqual=" + DEFAULT_TOTAL_PROCESSED_TASK);

        // Get all the instanceList where totalProcessedTask is greater than or equal to UPDATED_TOTAL_PROCESSED_TASK
        defaultInstanceShouldNotBeFound("totalProcessedTask.greaterThanOrEqual=" + UPDATED_TOTAL_PROCESSED_TASK);
    }

    @Test
    @Transactional
    void getAllInstancesByTotalProcessedTaskIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where totalProcessedTask is less than or equal to DEFAULT_TOTAL_PROCESSED_TASK
        defaultInstanceShouldBeFound("totalProcessedTask.lessThanOrEqual=" + DEFAULT_TOTAL_PROCESSED_TASK);

        // Get all the instanceList where totalProcessedTask is less than or equal to SMALLER_TOTAL_PROCESSED_TASK
        defaultInstanceShouldNotBeFound("totalProcessedTask.lessThanOrEqual=" + SMALLER_TOTAL_PROCESSED_TASK);
    }

    @Test
    @Transactional
    void getAllInstancesByTotalProcessedTaskIsLessThanSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where totalProcessedTask is less than DEFAULT_TOTAL_PROCESSED_TASK
        defaultInstanceShouldNotBeFound("totalProcessedTask.lessThan=" + DEFAULT_TOTAL_PROCESSED_TASK);

        // Get all the instanceList where totalProcessedTask is less than UPDATED_TOTAL_PROCESSED_TASK
        defaultInstanceShouldBeFound("totalProcessedTask.lessThan=" + UPDATED_TOTAL_PROCESSED_TASK);
    }

    @Test
    @Transactional
    void getAllInstancesByTotalProcessedTaskIsGreaterThanSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where totalProcessedTask is greater than DEFAULT_TOTAL_PROCESSED_TASK
        defaultInstanceShouldNotBeFound("totalProcessedTask.greaterThan=" + DEFAULT_TOTAL_PROCESSED_TASK);

        // Get all the instanceList where totalProcessedTask is greater than SMALLER_TOTAL_PROCESSED_TASK
        defaultInstanceShouldBeFound("totalProcessedTask.greaterThan=" + SMALLER_TOTAL_PROCESSED_TASK);
    }

    @Test
    @Transactional
    void getAllInstancesByProcessedStatisticsIsEqualToSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where processedStatistics equals to DEFAULT_PROCESSED_STATISTICS
        defaultInstanceShouldBeFound("processedStatistics.equals=" + DEFAULT_PROCESSED_STATISTICS);

        // Get all the instanceList where processedStatistics equals to UPDATED_PROCESSED_STATISTICS
        defaultInstanceShouldNotBeFound("processedStatistics.equals=" + UPDATED_PROCESSED_STATISTICS);
    }

    @Test
    @Transactional
    void getAllInstancesByProcessedStatisticsIsInShouldWork() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where processedStatistics in DEFAULT_PROCESSED_STATISTICS or UPDATED_PROCESSED_STATISTICS
        defaultInstanceShouldBeFound("processedStatistics.in=" + DEFAULT_PROCESSED_STATISTICS + "," + UPDATED_PROCESSED_STATISTICS);

        // Get all the instanceList where processedStatistics equals to UPDATED_PROCESSED_STATISTICS
        defaultInstanceShouldNotBeFound("processedStatistics.in=" + UPDATED_PROCESSED_STATISTICS);
    }

    @Test
    @Transactional
    void getAllInstancesByProcessedStatisticsIsNullOrNotNull() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where processedStatistics is not null
        defaultInstanceShouldBeFound("processedStatistics.specified=true");

        // Get all the instanceList where processedStatistics is null
        defaultInstanceShouldNotBeFound("processedStatistics.specified=false");
    }

    @Test
    @Transactional
    void getAllInstancesByProcessedStatisticsContainsSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where processedStatistics contains DEFAULT_PROCESSED_STATISTICS
        defaultInstanceShouldBeFound("processedStatistics.contains=" + DEFAULT_PROCESSED_STATISTICS);

        // Get all the instanceList where processedStatistics contains UPDATED_PROCESSED_STATISTICS
        defaultInstanceShouldNotBeFound("processedStatistics.contains=" + UPDATED_PROCESSED_STATISTICS);
    }

    @Test
    @Transactional
    void getAllInstancesByProcessedStatisticsNotContainsSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where processedStatistics does not contain DEFAULT_PROCESSED_STATISTICS
        defaultInstanceShouldNotBeFound("processedStatistics.doesNotContain=" + DEFAULT_PROCESSED_STATISTICS);

        // Get all the instanceList where processedStatistics does not contain UPDATED_PROCESSED_STATISTICS
        defaultInstanceShouldBeFound("processedStatistics.doesNotContain=" + UPDATED_PROCESSED_STATISTICS);
    }

    @Test
    @Transactional
    void getAllInstancesByHostNameIsEqualToSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where hostName equals to DEFAULT_HOST_NAME
        defaultInstanceShouldBeFound("hostName.equals=" + DEFAULT_HOST_NAME);

        // Get all the instanceList where hostName equals to UPDATED_HOST_NAME
        defaultInstanceShouldNotBeFound("hostName.equals=" + UPDATED_HOST_NAME);
    }

    @Test
    @Transactional
    void getAllInstancesByHostNameIsInShouldWork() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where hostName in DEFAULT_HOST_NAME or UPDATED_HOST_NAME
        defaultInstanceShouldBeFound("hostName.in=" + DEFAULT_HOST_NAME + "," + UPDATED_HOST_NAME);

        // Get all the instanceList where hostName equals to UPDATED_HOST_NAME
        defaultInstanceShouldNotBeFound("hostName.in=" + UPDATED_HOST_NAME);
    }

    @Test
    @Transactional
    void getAllInstancesByHostNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where hostName is not null
        defaultInstanceShouldBeFound("hostName.specified=true");

        // Get all the instanceList where hostName is null
        defaultInstanceShouldNotBeFound("hostName.specified=false");
    }

    @Test
    @Transactional
    void getAllInstancesByHostNameContainsSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where hostName contains DEFAULT_HOST_NAME
        defaultInstanceShouldBeFound("hostName.contains=" + DEFAULT_HOST_NAME);

        // Get all the instanceList where hostName contains UPDATED_HOST_NAME
        defaultInstanceShouldNotBeFound("hostName.contains=" + UPDATED_HOST_NAME);
    }

    @Test
    @Transactional
    void getAllInstancesByHostNameNotContainsSomething() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        // Get all the instanceList where hostName does not contain DEFAULT_HOST_NAME
        defaultInstanceShouldNotBeFound("hostName.doesNotContain=" + DEFAULT_HOST_NAME);

        // Get all the instanceList where hostName does not contain UPDATED_HOST_NAME
        defaultInstanceShouldBeFound("hostName.doesNotContain=" + UPDATED_HOST_NAME);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultInstanceShouldBeFound(String filter) throws Exception {
        restInstanceMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(instanceEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].moduleName").value(hasItem(DEFAULT_MODULE_NAME)))
            .andExpect(jsonPath("$.[*].port").value(hasItem(DEFAULT_PORT)))
            .andExpect(jsonPath("$.[*].moduleStatus").value(hasItem(DEFAULT_MODULE_STATUS)))
            .andExpect(jsonPath("$.[*].lastUpdateDate").value(hasItem(DEFAULT_LAST_UPDATE_DATE.toString())))
            .andExpect(jsonPath("$.[*].threadPoolQueueSize").value(hasItem(DEFAULT_THREAD_POOL_QUEUE_SIZE.intValue())))
            .andExpect(jsonPath("$.[*].moduleStartTime").value(hasItem(DEFAULT_MODULE_START_TIME.intValue())))
            .andExpect(jsonPath("$.[*].totalProcessedWork").value(hasItem(DEFAULT_TOTAL_PROCESSED_WORK.intValue())))
            .andExpect(jsonPath("$.[*].totalProcessedTask").value(hasItem(DEFAULT_TOTAL_PROCESSED_TASK.intValue())))
            .andExpect(jsonPath("$.[*].processedStatistics").value(hasItem(DEFAULT_PROCESSED_STATISTICS)))
            .andExpect(jsonPath("$.[*].hostName").value(hasItem(DEFAULT_HOST_NAME)));

        // Check, that the count call also returns 1
        restInstanceMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultInstanceShouldNotBeFound(String filter) throws Exception {
        restInstanceMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restInstanceMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingInstance() throws Exception {
        // Get the instance
        restInstanceMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingInstance() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        int databaseSizeBeforeUpdate = instanceRepository.findAll().size();

        // Update the instance
        InstanceEntity updatedInstanceEntity = instanceRepository.findById(instanceEntity.getId()).get();
        // Disconnect from session so that the updates on updatedInstanceEntity are not directly saved in db
        em.detach(updatedInstanceEntity);
        updatedInstanceEntity
            .moduleName(UPDATED_MODULE_NAME)
            .port(UPDATED_PORT)
            .moduleStatus(UPDATED_MODULE_STATUS)
            .lastUpdateDate(UPDATED_LAST_UPDATE_DATE)
            .threadPoolQueueSize(UPDATED_THREAD_POOL_QUEUE_SIZE)
            .moduleStartTime(UPDATED_MODULE_START_TIME)
            .totalProcessedWork(UPDATED_TOTAL_PROCESSED_WORK)
            .totalProcessedTask(UPDATED_TOTAL_PROCESSED_TASK)
            .processedStatistics(UPDATED_PROCESSED_STATISTICS)
            .hostName(UPDATED_HOST_NAME);
        InstanceDTO instanceDTO = instanceMapper.toDto(updatedInstanceEntity);

        restInstanceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, instanceDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(instanceDTO))
            )
            .andExpect(status().isOk());

        // Validate the Instance in the database
        List<InstanceEntity> instanceList = instanceRepository.findAll();
        assertThat(instanceList).hasSize(databaseSizeBeforeUpdate);
        InstanceEntity testInstance = instanceList.get(instanceList.size() - 1);
        assertThat(testInstance.getModuleName()).isEqualTo(UPDATED_MODULE_NAME);
        assertThat(testInstance.getPort()).isEqualTo(UPDATED_PORT);
        assertThat(testInstance.getModuleStatus()).isEqualTo(UPDATED_MODULE_STATUS);
        assertThat(testInstance.getLastUpdateDate()).isEqualTo(UPDATED_LAST_UPDATE_DATE);
        assertThat(testInstance.getThreadPoolQueueSize()).isEqualTo(UPDATED_THREAD_POOL_QUEUE_SIZE);
        assertThat(testInstance.getModuleStartTime()).isEqualTo(UPDATED_MODULE_START_TIME);
        assertThat(testInstance.getTotalProcessedWork()).isEqualTo(UPDATED_TOTAL_PROCESSED_WORK);
        assertThat(testInstance.getTotalProcessedTask()).isEqualTo(UPDATED_TOTAL_PROCESSED_TASK);
        assertThat(testInstance.getProcessedStatistics()).isEqualTo(UPDATED_PROCESSED_STATISTICS);
        assertThat(testInstance.getHostName()).isEqualTo(UPDATED_HOST_NAME);
    }

    @Test
    @Transactional
    void putNonExistingInstance() throws Exception {
        int databaseSizeBeforeUpdate = instanceRepository.findAll().size();
        instanceEntity.setId(count.incrementAndGet());

        // Create the Instance
        InstanceDTO instanceDTO = instanceMapper.toDto(instanceEntity);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restInstanceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, instanceDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(instanceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Instance in the database
        List<InstanceEntity> instanceList = instanceRepository.findAll();
        assertThat(instanceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchInstance() throws Exception {
        int databaseSizeBeforeUpdate = instanceRepository.findAll().size();
        instanceEntity.setId(count.incrementAndGet());

        // Create the Instance
        InstanceDTO instanceDTO = instanceMapper.toDto(instanceEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInstanceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(instanceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Instance in the database
        List<InstanceEntity> instanceList = instanceRepository.findAll();
        assertThat(instanceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamInstance() throws Exception {
        int databaseSizeBeforeUpdate = instanceRepository.findAll().size();
        instanceEntity.setId(count.incrementAndGet());

        // Create the Instance
        InstanceDTO instanceDTO = instanceMapper.toDto(instanceEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInstanceMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(instanceDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Instance in the database
        List<InstanceEntity> instanceList = instanceRepository.findAll();
        assertThat(instanceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateInstanceWithPatch() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        int databaseSizeBeforeUpdate = instanceRepository.findAll().size();

        // Update the instance using partial update
        InstanceEntity partialUpdatedInstanceEntity = new InstanceEntity();
        partialUpdatedInstanceEntity.setId(instanceEntity.getId());

        partialUpdatedInstanceEntity
            .moduleName(UPDATED_MODULE_NAME)
            .moduleStatus(UPDATED_MODULE_STATUS)
            .lastUpdateDate(UPDATED_LAST_UPDATE_DATE)
            .threadPoolQueueSize(UPDATED_THREAD_POOL_QUEUE_SIZE)
            .moduleStartTime(UPDATED_MODULE_START_TIME)
            .totalProcessedWork(UPDATED_TOTAL_PROCESSED_WORK)
            .hostName(UPDATED_HOST_NAME);

        restInstanceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedInstanceEntity.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedInstanceEntity))
            )
            .andExpect(status().isOk());

        // Validate the Instance in the database
        List<InstanceEntity> instanceList = instanceRepository.findAll();
        assertThat(instanceList).hasSize(databaseSizeBeforeUpdate);
        InstanceEntity testInstance = instanceList.get(instanceList.size() - 1);
        assertThat(testInstance.getModuleName()).isEqualTo(UPDATED_MODULE_NAME);
        assertThat(testInstance.getPort()).isEqualTo(DEFAULT_PORT);
        assertThat(testInstance.getModuleStatus()).isEqualTo(UPDATED_MODULE_STATUS);
        assertThat(testInstance.getLastUpdateDate()).isEqualTo(UPDATED_LAST_UPDATE_DATE);
        assertThat(testInstance.getThreadPoolQueueSize()).isEqualTo(UPDATED_THREAD_POOL_QUEUE_SIZE);
        assertThat(testInstance.getModuleStartTime()).isEqualTo(UPDATED_MODULE_START_TIME);
        assertThat(testInstance.getTotalProcessedWork()).isEqualTo(UPDATED_TOTAL_PROCESSED_WORK);
        assertThat(testInstance.getTotalProcessedTask()).isEqualTo(DEFAULT_TOTAL_PROCESSED_TASK);
        assertThat(testInstance.getProcessedStatistics()).isEqualTo(DEFAULT_PROCESSED_STATISTICS);
        assertThat(testInstance.getHostName()).isEqualTo(UPDATED_HOST_NAME);
    }

    @Test
    @Transactional
    void fullUpdateInstanceWithPatch() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        int databaseSizeBeforeUpdate = instanceRepository.findAll().size();

        // Update the instance using partial update
        InstanceEntity partialUpdatedInstanceEntity = new InstanceEntity();
        partialUpdatedInstanceEntity.setId(instanceEntity.getId());

        partialUpdatedInstanceEntity
            .moduleName(UPDATED_MODULE_NAME)
            .port(UPDATED_PORT)
            .moduleStatus(UPDATED_MODULE_STATUS)
            .lastUpdateDate(UPDATED_LAST_UPDATE_DATE)
            .threadPoolQueueSize(UPDATED_THREAD_POOL_QUEUE_SIZE)
            .moduleStartTime(UPDATED_MODULE_START_TIME)
            .totalProcessedWork(UPDATED_TOTAL_PROCESSED_WORK)
            .totalProcessedTask(UPDATED_TOTAL_PROCESSED_TASK)
            .processedStatistics(UPDATED_PROCESSED_STATISTICS)
            .hostName(UPDATED_HOST_NAME);

        restInstanceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedInstanceEntity.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedInstanceEntity))
            )
            .andExpect(status().isOk());

        // Validate the Instance in the database
        List<InstanceEntity> instanceList = instanceRepository.findAll();
        assertThat(instanceList).hasSize(databaseSizeBeforeUpdate);
        InstanceEntity testInstance = instanceList.get(instanceList.size() - 1);
        assertThat(testInstance.getModuleName()).isEqualTo(UPDATED_MODULE_NAME);
        assertThat(testInstance.getPort()).isEqualTo(UPDATED_PORT);
        assertThat(testInstance.getModuleStatus()).isEqualTo(UPDATED_MODULE_STATUS);
        assertThat(testInstance.getLastUpdateDate()).isEqualTo(UPDATED_LAST_UPDATE_DATE);
        assertThat(testInstance.getThreadPoolQueueSize()).isEqualTo(UPDATED_THREAD_POOL_QUEUE_SIZE);
        assertThat(testInstance.getModuleStartTime()).isEqualTo(UPDATED_MODULE_START_TIME);
        assertThat(testInstance.getTotalProcessedWork()).isEqualTo(UPDATED_TOTAL_PROCESSED_WORK);
        assertThat(testInstance.getTotalProcessedTask()).isEqualTo(UPDATED_TOTAL_PROCESSED_TASK);
        assertThat(testInstance.getProcessedStatistics()).isEqualTo(UPDATED_PROCESSED_STATISTICS);
        assertThat(testInstance.getHostName()).isEqualTo(UPDATED_HOST_NAME);
    }

    @Test
    @Transactional
    void patchNonExistingInstance() throws Exception {
        int databaseSizeBeforeUpdate = instanceRepository.findAll().size();
        instanceEntity.setId(count.incrementAndGet());

        // Create the Instance
        InstanceDTO instanceDTO = instanceMapper.toDto(instanceEntity);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restInstanceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, instanceDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(instanceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Instance in the database
        List<InstanceEntity> instanceList = instanceRepository.findAll();
        assertThat(instanceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchInstance() throws Exception {
        int databaseSizeBeforeUpdate = instanceRepository.findAll().size();
        instanceEntity.setId(count.incrementAndGet());

        // Create the Instance
        InstanceDTO instanceDTO = instanceMapper.toDto(instanceEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInstanceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(instanceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Instance in the database
        List<InstanceEntity> instanceList = instanceRepository.findAll();
        assertThat(instanceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamInstance() throws Exception {
        int databaseSizeBeforeUpdate = instanceRepository.findAll().size();
        instanceEntity.setId(count.incrementAndGet());

        // Create the Instance
        InstanceDTO instanceDTO = instanceMapper.toDto(instanceEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInstanceMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(instanceDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Instance in the database
        List<InstanceEntity> instanceList = instanceRepository.findAll();
        assertThat(instanceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteInstance() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instanceEntity);

        int databaseSizeBeforeDelete = instanceRepository.findAll().size();

        // Delete the instance
        restInstanceMockMvc
            .perform(delete(ENTITY_API_URL_ID, instanceEntity.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<InstanceEntity> instanceList = instanceRepository.findAll();
        assertThat(instanceList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
