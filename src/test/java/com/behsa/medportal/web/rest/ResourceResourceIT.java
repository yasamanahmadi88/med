/*
package com.behsa.medportal.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.behsa.medportal.IntegrationTest;
import com.behsa.medportal.domain.ResourceAuthorityEntity;
import com.behsa.medportal.domain.ResourceEntity;
import com.behsa.medportal.repository.ResourceRepository;
import com.behsa.medportal.service.criteria.ResourceCriteria;
import com.behsa.medportal.service.dto.ResourceDTO;
import com.behsa.medportal.service.mapper.ResourceMapper;
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

*/
/**
 * Integration tests for the {@link ResourceResource} REST controller.
 *//*

@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ResourceResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DISPLAY_NAME = "AAAAAAAAAA";
    private static final String UPDATED_DISPLAY_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_API_URI = "AAAAAAAAAA";
    private static final String UPDATED_API_URI = "BBBBBBBBBB";

    private static final String DEFAULT_RESOURCE_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_RESOURCE_TYPE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/resources";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restResourceMockMvc;

    private ResourceEntity resourceEntity;

    */
/**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     *//*

    public static ResourceEntity createEntity(EntityManager em) {
        ResourceEntity resourceEntity = new ResourceEntity()
            .name(DEFAULT_NAME)
            .displayName(DEFAULT_DISPLAY_NAME)
            .apiUri(DEFAULT_API_URI)
            .resourceType(DEFAULT_RESOURCE_TYPE);
        return resourceEntity;
    }

    */
/**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     *//*

    public static ResourceEntity createUpdatedEntity(EntityManager em) {
        ResourceEntity resourceEntity = new ResourceEntity()
            .name(UPDATED_NAME)
            .displayName(UPDATED_DISPLAY_NAME)
            .apiUri(UPDATED_API_URI)
            .resourceType(UPDATED_RESOURCE_TYPE);
        return resourceEntity;
    }

    @BeforeEach
    public void initTest() {
        resourceEntity = createEntity(em);
    }

    @Test
    @Transactional
    void createResource() throws Exception {
        int databaseSizeBeforeCreate = resourceRepository.findAll().size();
        // Create the Resource
        ResourceDTO resourceDTO = resourceMapper.toDto(resourceEntity);
        restResourceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(resourceDTO)))
            .andExpect(status().isCreated());

        // Validate the Resource in the database
        List<ResourceEntity> resourceList = resourceRepository.findAll();
        assertThat(resourceList).hasSize(databaseSizeBeforeCreate + 1);
        ResourceEntity testResource = resourceList.get(resourceList.size() - 1);
        assertThat(testResource.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testResource.getDisplayName()).isEqualTo(DEFAULT_DISPLAY_NAME);
        assertThat(testResource.getApiUri()).isEqualTo(DEFAULT_API_URI);
        assertThat(testResource.getResourceType()).isEqualTo(DEFAULT_RESOURCE_TYPE);
    }

    @Test
    @Transactional
    void createResourceWithExistingId() throws Exception {
        // Create the Resource with an existing ID
        resourceEntity.setId(1L);
        ResourceDTO resourceDTO = resourceMapper.toDto(resourceEntity);

        int databaseSizeBeforeCreate = resourceRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restResourceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(resourceDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Resource in the database
        List<ResourceEntity> resourceList = resourceRepository.findAll();
        assertThat(resourceList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = resourceRepository.findAll().size();
        // set the field null
        resourceEntity.setName(null);

        // Create the Resource, which fails.
        ResourceDTO resourceDTO = resourceMapper.toDto(resourceEntity);

        restResourceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(resourceDTO)))
            .andExpect(status().isBadRequest());

        List<ResourceEntity> resourceList = resourceRepository.findAll();
        assertThat(resourceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkDisplayNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = resourceRepository.findAll().size();
        // set the field null
        resourceEntity.setDisplayName(null);

        // Create the Resource, which fails.
        ResourceDTO resourceDTO = resourceMapper.toDto(resourceEntity);

        restResourceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(resourceDTO)))
            .andExpect(status().isBadRequest());

        List<ResourceEntity> resourceList = resourceRepository.findAll();
        assertThat(resourceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkApiUriIsRequired() throws Exception {
        int databaseSizeBeforeTest = resourceRepository.findAll().size();
        // set the field null
        resourceEntity.setApiUri(null);

        // Create the Resource, which fails.
        ResourceDTO resourceDTO = resourceMapper.toDto(resourceEntity);

        restResourceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(resourceDTO)))
            .andExpect(status().isBadRequest());

        List<ResourceEntity> resourceList = resourceRepository.findAll();
        assertThat(resourceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkResourceTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = resourceRepository.findAll().size();
        // set the field null
        resourceEntity.setResourceType(null);

        // Create the Resource, which fails.
        ResourceDTO resourceDTO = resourceMapper.toDto(resourceEntity);

        restResourceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(resourceDTO)))
            .andExpect(status().isBadRequest());

        List<ResourceEntity> resourceList = resourceRepository.findAll();
        assertThat(resourceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllResources() throws Exception {
        // Initialize the database
        resourceRepository.saveAndFlush(resourceEntity);

        // Get all the resourceList
        restResourceMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(resourceEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].displayName").value(hasItem(DEFAULT_DISPLAY_NAME)))
            .andExpect(jsonPath("$.[*].apiUri").value(hasItem(DEFAULT_API_URI)))
            .andExpect(jsonPath("$.[*].resourceType").value(hasItem(DEFAULT_RESOURCE_TYPE)));
    }

    @Test
    @Transactional
    void getResource() throws Exception {
        // Initialize the database
        resourceRepository.saveAndFlush(resourceEntity);

        // Get the resource
        restResourceMockMvc
            .perform(get(ENTITY_API_URL_ID, resourceEntity.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(resourceEntity.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.displayName").value(DEFAULT_DISPLAY_NAME))
            .andExpect(jsonPath("$.apiUri").value(DEFAULT_API_URI))
            .andExpect(jsonPath("$.resourceType").value(DEFAULT_RESOURCE_TYPE));
    }

    @Test
    @Transactional
    void getResourcesByIdFiltering() throws Exception {
        // Initialize the database
        resourceRepository.saveAndFlush(resourceEntity);

        Long id = resourceEntity.getId();

        defaultResourceShouldBeFound("id.equals=" + id);
        defaultResourceShouldNotBeFound("id.notEquals=" + id);

        defaultResourceShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultResourceShouldNotBeFound("id.greaterThan=" + id);

        defaultResourceShouldBeFound("id.lessThanOrEqual=" + id);
        defaultResourceShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllResourcesByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        resourceRepository.saveAndFlush(resourceEntity);

        // Get all the resourceList where name equals to DEFAULT_NAME
        defaultResourceShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the resourceList where name equals to UPDATED_NAME
        defaultResourceShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllResourcesByNameIsInShouldWork() throws Exception {
        // Initialize the database
        resourceRepository.saveAndFlush(resourceEntity);

        // Get all the resourceList where name in DEFAULT_NAME or UPDATED_NAME
        defaultResourceShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the resourceList where name equals to UPDATED_NAME
        defaultResourceShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllResourcesByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        resourceRepository.saveAndFlush(resourceEntity);

        // Get all the resourceList where name is not null
        defaultResourceShouldBeFound("name.specified=true");

        // Get all the resourceList where name is null
        defaultResourceShouldNotBeFound("name.specified=false");
    }

    @Test
    @Transactional
    void getAllResourcesByNameContainsSomething() throws Exception {
        // Initialize the database
        resourceRepository.saveAndFlush(resourceEntity);

        // Get all the resourceList where name contains DEFAULT_NAME
        defaultResourceShouldBeFound("name.contains=" + DEFAULT_NAME);

        // Get all the resourceList where name contains UPDATED_NAME
        defaultResourceShouldNotBeFound("name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllResourcesByNameNotContainsSomething() throws Exception {
        // Initialize the database
        resourceRepository.saveAndFlush(resourceEntity);

        // Get all the resourceList where name does not contain DEFAULT_NAME
        defaultResourceShouldNotBeFound("name.doesNotContain=" + DEFAULT_NAME);

        // Get all the resourceList where name does not contain UPDATED_NAME
        defaultResourceShouldBeFound("name.doesNotContain=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllResourcesByDisplayNameIsEqualToSomething() throws Exception {
        // Initialize the database
        resourceRepository.saveAndFlush(resourceEntity);

        // Get all the resourceList where displayName equals to DEFAULT_DISPLAY_NAME
        defaultResourceShouldBeFound("displayName.equals=" + DEFAULT_DISPLAY_NAME);

        // Get all the resourceList where displayName equals to UPDATED_DISPLAY_NAME
        defaultResourceShouldNotBeFound("displayName.equals=" + UPDATED_DISPLAY_NAME);
    }

    @Test
    @Transactional
    void getAllResourcesByDisplayNameIsInShouldWork() throws Exception {
        // Initialize the database
        resourceRepository.saveAndFlush(resourceEntity);

        // Get all the resourceList where displayName in DEFAULT_DISPLAY_NAME or UPDATED_DISPLAY_NAME
        defaultResourceShouldBeFound("displayName.in=" + DEFAULT_DISPLAY_NAME + "," + UPDATED_DISPLAY_NAME);

        // Get all the resourceList where displayName equals to UPDATED_DISPLAY_NAME
        defaultResourceShouldNotBeFound("displayName.in=" + UPDATED_DISPLAY_NAME);
    }

    @Test
    @Transactional
    void getAllResourcesByDisplayNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        resourceRepository.saveAndFlush(resourceEntity);

        // Get all the resourceList where displayName is not null
        defaultResourceShouldBeFound("displayName.specified=true");

        // Get all the resourceList where displayName is null
        defaultResourceShouldNotBeFound("displayName.specified=false");
    }

    @Test
    @Transactional
    void getAllResourcesByDisplayNameContainsSomething() throws Exception {
        // Initialize the database
        resourceRepository.saveAndFlush(resourceEntity);

        // Get all the resourceList where displayName contains DEFAULT_DISPLAY_NAME
        defaultResourceShouldBeFound("displayName.contains=" + DEFAULT_DISPLAY_NAME);

        // Get all the resourceList where displayName contains UPDATED_DISPLAY_NAME
        defaultResourceShouldNotBeFound("displayName.contains=" + UPDATED_DISPLAY_NAME);
    }

    @Test
    @Transactional
    void getAllResourcesByDisplayNameNotContainsSomething() throws Exception {
        // Initialize the database
        resourceRepository.saveAndFlush(resourceEntity);

        // Get all the resourceList where displayName does not contain DEFAULT_DISPLAY_NAME
        defaultResourceShouldNotBeFound("displayName.doesNotContain=" + DEFAULT_DISPLAY_NAME);

        // Get all the resourceList where displayName does not contain UPDATED_DISPLAY_NAME
        defaultResourceShouldBeFound("displayName.doesNotContain=" + UPDATED_DISPLAY_NAME);
    }

    @Test
    @Transactional
    void getAllResourcesByApiUriIsEqualToSomething() throws Exception {
        // Initialize the database
        resourceRepository.saveAndFlush(resourceEntity);

        // Get all the resourceList where apiUri equals to DEFAULT_API_URI
        defaultResourceShouldBeFound("apiUri.equals=" + DEFAULT_API_URI);

        // Get all the resourceList where apiUri equals to UPDATED_API_URI
        defaultResourceShouldNotBeFound("apiUri.equals=" + UPDATED_API_URI);
    }

    @Test
    @Transactional
    void getAllResourcesByApiUriIsInShouldWork() throws Exception {
        // Initialize the database
        resourceRepository.saveAndFlush(resourceEntity);

        // Get all the resourceList where apiUri in DEFAULT_API_URI or UPDATED_API_URI
        defaultResourceShouldBeFound("apiUri.in=" + DEFAULT_API_URI + "," + UPDATED_API_URI);

        // Get all the resourceList where apiUri equals to UPDATED_API_URI
        defaultResourceShouldNotBeFound("apiUri.in=" + UPDATED_API_URI);
    }

    @Test
    @Transactional
    void getAllResourcesByApiUriIsNullOrNotNull() throws Exception {
        // Initialize the database
        resourceRepository.saveAndFlush(resourceEntity);

        // Get all the resourceList where apiUri is not null
        defaultResourceShouldBeFound("apiUri.specified=true");

        // Get all the resourceList where apiUri is null
        defaultResourceShouldNotBeFound("apiUri.specified=false");
    }

    @Test
    @Transactional
    void getAllResourcesByApiUriContainsSomething() throws Exception {
        // Initialize the database
        resourceRepository.saveAndFlush(resourceEntity);

        // Get all the resourceList where apiUri contains DEFAULT_API_URI
        defaultResourceShouldBeFound("apiUri.contains=" + DEFAULT_API_URI);

        // Get all the resourceList where apiUri contains UPDATED_API_URI
        defaultResourceShouldNotBeFound("apiUri.contains=" + UPDATED_API_URI);
    }

    @Test
    @Transactional
    void getAllResourcesByApiUriNotContainsSomething() throws Exception {
        // Initialize the database
        resourceRepository.saveAndFlush(resourceEntity);

        // Get all the resourceList where apiUri does not contain DEFAULT_API_URI
        defaultResourceShouldNotBeFound("apiUri.doesNotContain=" + DEFAULT_API_URI);

        // Get all the resourceList where apiUri does not contain UPDATED_API_URI
        defaultResourceShouldBeFound("apiUri.doesNotContain=" + UPDATED_API_URI);
    }

    @Test
    @Transactional
    void getAllResourcesByResourceTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        resourceRepository.saveAndFlush(resourceEntity);

        // Get all the resourceList where resourceType equals to DEFAULT_RESOURCE_TYPE
        defaultResourceShouldBeFound("resourceType.equals=" + DEFAULT_RESOURCE_TYPE);

        // Get all the resourceList where resourceType equals to UPDATED_RESOURCE_TYPE
        defaultResourceShouldNotBeFound("resourceType.equals=" + UPDATED_RESOURCE_TYPE);
    }

    @Test
    @Transactional
    void getAllResourcesByResourceTypeIsInShouldWork() throws Exception {
        // Initialize the database
        resourceRepository.saveAndFlush(resourceEntity);

        // Get all the resourceList where resourceType in DEFAULT_RESOURCE_TYPE or UPDATED_RESOURCE_TYPE
        defaultResourceShouldBeFound("resourceType.in=" + DEFAULT_RESOURCE_TYPE + "," + UPDATED_RESOURCE_TYPE);

        // Get all the resourceList where resourceType equals to UPDATED_RESOURCE_TYPE
        defaultResourceShouldNotBeFound("resourceType.in=" + UPDATED_RESOURCE_TYPE);
    }

    @Test
    @Transactional
    void getAllResourcesByResourceTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        resourceRepository.saveAndFlush(resourceEntity);

        // Get all the resourceList where resourceType is not null
        defaultResourceShouldBeFound("resourceType.specified=true");

        // Get all the resourceList where resourceType is null
        defaultResourceShouldNotBeFound("resourceType.specified=false");
    }

    @Test
    @Transactional
    void getAllResourcesByResourceTypeContainsSomething() throws Exception {
        // Initialize the database
        resourceRepository.saveAndFlush(resourceEntity);

        // Get all the resourceList where resourceType contains DEFAULT_RESOURCE_TYPE
        defaultResourceShouldBeFound("resourceType.contains=" + DEFAULT_RESOURCE_TYPE);

        // Get all the resourceList where resourceType contains UPDATED_RESOURCE_TYPE
        defaultResourceShouldNotBeFound("resourceType.contains=" + UPDATED_RESOURCE_TYPE);
    }

    @Test
    @Transactional
    void getAllResourcesByResourceTypeNotContainsSomething() throws Exception {
        // Initialize the database
        resourceRepository.saveAndFlush(resourceEntity);

        // Get all the resourceList where resourceType does not contain DEFAULT_RESOURCE_TYPE
        defaultResourceShouldNotBeFound("resourceType.doesNotContain=" + DEFAULT_RESOURCE_TYPE);

        // Get all the resourceList where resourceType does not contain UPDATED_RESOURCE_TYPE
        defaultResourceShouldBeFound("resourceType.doesNotContain=" + UPDATED_RESOURCE_TYPE);
    }

    @Test
    @Transactional
    void getAllResourcesByResourceAuthoritiesIsEqualToSomething() throws Exception {
        ResourceAuthorityEntity resourceAuthorities;
        if (TestUtil.findAll(em, ResourceAuthorityEntity.class).isEmpty()) {
            resourceRepository.saveAndFlush(resourceEntity);
            resourceAuthorities = ResourceAuthorityResourceIT.createEntity(em);
        } else {
            resourceAuthorities = TestUtil.findAll(em, ResourceAuthorityEntity.class).get(0);
        }
        em.persist(resourceAuthorities);
        em.flush();
        resourceEntity.addResourceAuthorities(resourceAuthorities);
        resourceRepository.saveAndFlush(resourceEntity);
        Long resourceAuthoritiesId = resourceAuthorities.getId();

        // Get all the resourceList where resourceAuthorities equals to resourceAuthoritiesId
        defaultResourceShouldBeFound("resourceAuthoritiesId.equals=" + resourceAuthoritiesId);

        // Get all the resourceList where resourceAuthorities equals to (resourceAuthoritiesId + 1)
        defaultResourceShouldNotBeFound("resourceAuthoritiesId.equals=" + (resourceAuthoritiesId + 1));
    }

    */
/**
     * Executes the search, and checks that the default entity is returned.
     *//*

    private void defaultResourceShouldBeFound(String filter) throws Exception {
        restResourceMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(resourceEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].displayName").value(hasItem(DEFAULT_DISPLAY_NAME)))
            .andExpect(jsonPath("$.[*].apiUri").value(hasItem(DEFAULT_API_URI)))
            .andExpect(jsonPath("$.[*].resourceType").value(hasItem(DEFAULT_RESOURCE_TYPE)));

        // Check, that the count call also returns 1
        restResourceMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    */
/**
     * Executes the search, and checks that the default entity is not returned.
     *//*

    private void defaultResourceShouldNotBeFound(String filter) throws Exception {
        restResourceMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restResourceMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingResource() throws Exception {
        // Get the resource
        restResourceMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingResource() throws Exception {
        // Initialize the database
        resourceRepository.saveAndFlush(resourceEntity);

        int databaseSizeBeforeUpdate = resourceRepository.findAll().size();

        // Update the resource
        ResourceEntity updatedResourceEntity = resourceRepository.findById(resourceEntity.getId()).get();
        // Disconnect from session so that the updates on updatedResourceEntity are not directly saved in db
        em.detach(updatedResourceEntity);
        updatedResourceEntity
            .name(UPDATED_NAME)
            .displayName(UPDATED_DISPLAY_NAME)
            .apiUri(UPDATED_API_URI)
            .resourceType(UPDATED_RESOURCE_TYPE);
        ResourceDTO resourceDTO = resourceMapper.toDto(updatedResourceEntity);

        restResourceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, resourceDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(resourceDTO))
            )
            .andExpect(status().isOk());

        // Validate the Resource in the database
        List<ResourceEntity> resourceList = resourceRepository.findAll();
        assertThat(resourceList).hasSize(databaseSizeBeforeUpdate);
        ResourceEntity testResource = resourceList.get(resourceList.size() - 1);
        assertThat(testResource.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testResource.getDisplayName()).isEqualTo(UPDATED_DISPLAY_NAME);
        assertThat(testResource.getApiUri()).isEqualTo(UPDATED_API_URI);
        assertThat(testResource.getResourceType()).isEqualTo(UPDATED_RESOURCE_TYPE);
    }

    @Test
    @Transactional
    void putNonExistingResource() throws Exception {
        int databaseSizeBeforeUpdate = resourceRepository.findAll().size();
        resourceEntity.setId(count.incrementAndGet());

        // Create the Resource
        ResourceDTO resourceDTO = resourceMapper.toDto(resourceEntity);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restResourceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, resourceDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(resourceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Resource in the database
        List<ResourceEntity> resourceList = resourceRepository.findAll();
        assertThat(resourceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchResource() throws Exception {
        int databaseSizeBeforeUpdate = resourceRepository.findAll().size();
        resourceEntity.setId(count.incrementAndGet());

        // Create the Resource
        ResourceDTO resourceDTO = resourceMapper.toDto(resourceEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restResourceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(resourceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Resource in the database
        List<ResourceEntity> resourceList = resourceRepository.findAll();
        assertThat(resourceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamResource() throws Exception {
        int databaseSizeBeforeUpdate = resourceRepository.findAll().size();
        resourceEntity.setId(count.incrementAndGet());

        // Create the Resource
        ResourceDTO resourceDTO = resourceMapper.toDto(resourceEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restResourceMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(resourceDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Resource in the database
        List<ResourceEntity> resourceList = resourceRepository.findAll();
        assertThat(resourceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateResourceWithPatch() throws Exception {
        // Initialize the database
        resourceRepository.saveAndFlush(resourceEntity);

        int databaseSizeBeforeUpdate = resourceRepository.findAll().size();

        // Update the resource using partial update
        ResourceEntity partialUpdatedResourceEntity = new ResourceEntity();
        partialUpdatedResourceEntity.setId(resourceEntity.getId());

        partialUpdatedResourceEntity.displayName(UPDATED_DISPLAY_NAME);

        restResourceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedResourceEntity.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedResourceEntity))
            )
            .andExpect(status().isOk());

        // Validate the Resource in the database
        List<ResourceEntity> resourceList = resourceRepository.findAll();
        assertThat(resourceList).hasSize(databaseSizeBeforeUpdate);
        ResourceEntity testResource = resourceList.get(resourceList.size() - 1);
        assertThat(testResource.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testResource.getDisplayName()).isEqualTo(UPDATED_DISPLAY_NAME);
        assertThat(testResource.getApiUri()).isEqualTo(DEFAULT_API_URI);
        assertThat(testResource.getResourceType()).isEqualTo(DEFAULT_RESOURCE_TYPE);
    }

    @Test
    @Transactional
    void fullUpdateResourceWithPatch() throws Exception {
        // Initialize the database
        resourceRepository.saveAndFlush(resourceEntity);

        int databaseSizeBeforeUpdate = resourceRepository.findAll().size();

        // Update the resource using partial update
        ResourceEntity partialUpdatedResourceEntity = new ResourceEntity();
        partialUpdatedResourceEntity.setId(resourceEntity.getId());

        partialUpdatedResourceEntity
            .name(UPDATED_NAME)
            .displayName(UPDATED_DISPLAY_NAME)
            .apiUri(UPDATED_API_URI)
            .resourceType(UPDATED_RESOURCE_TYPE);

        restResourceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedResourceEntity.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedResourceEntity))
            )
            .andExpect(status().isOk());

        // Validate the Resource in the database
        List<ResourceEntity> resourceList = resourceRepository.findAll();
        assertThat(resourceList).hasSize(databaseSizeBeforeUpdate);
        ResourceEntity testResource = resourceList.get(resourceList.size() - 1);
        assertThat(testResource.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testResource.getDisplayName()).isEqualTo(UPDATED_DISPLAY_NAME);
        assertThat(testResource.getApiUri()).isEqualTo(UPDATED_API_URI);
        assertThat(testResource.getResourceType()).isEqualTo(UPDATED_RESOURCE_TYPE);
    }

    @Test
    @Transactional
    void patchNonExistingResource() throws Exception {
        int databaseSizeBeforeUpdate = resourceRepository.findAll().size();
        resourceEntity.setId(count.incrementAndGet());

        // Create the Resource
        ResourceDTO resourceDTO = resourceMapper.toDto(resourceEntity);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restResourceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, resourceDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(resourceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Resource in the database
        List<ResourceEntity> resourceList = resourceRepository.findAll();
        assertThat(resourceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchResource() throws Exception {
        int databaseSizeBeforeUpdate = resourceRepository.findAll().size();
        resourceEntity.setId(count.incrementAndGet());

        // Create the Resource
        ResourceDTO resourceDTO = resourceMapper.toDto(resourceEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restResourceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(resourceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Resource in the database
        List<ResourceEntity> resourceList = resourceRepository.findAll();
        assertThat(resourceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamResource() throws Exception {
        int databaseSizeBeforeUpdate = resourceRepository.findAll().size();
        resourceEntity.setId(count.incrementAndGet());

        // Create the Resource
        ResourceDTO resourceDTO = resourceMapper.toDto(resourceEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restResourceMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(resourceDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Resource in the database
        List<ResourceEntity> resourceList = resourceRepository.findAll();
        assertThat(resourceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteResource() throws Exception {
        // Initialize the database
        resourceRepository.saveAndFlush(resourceEntity);

        int databaseSizeBeforeDelete = resourceRepository.findAll().size();

        // Delete the resource
        restResourceMockMvc
            .perform(delete(ENTITY_API_URL_ID, resourceEntity.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<ResourceEntity> resourceList = resourceRepository.findAll();
        assertThat(resourceList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
*/
