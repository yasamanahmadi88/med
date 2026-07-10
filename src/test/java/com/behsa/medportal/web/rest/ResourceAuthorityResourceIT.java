/*
package com.behsa.medportal.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.behsa.medportal.IntegrationTest;
import com.behsa.medportal.domain.MedAuthorityEntity;
import com.behsa.medportal.domain.ResourceAuthorityEntity;
import com.behsa.medportal.domain.ResourceEntity;
import com.behsa.medportal.repository.ResourceAuthorityRepository;
import com.behsa.medportal.service.criteria.ResourceAuthorityCriteria;
import com.behsa.medportal.service.dto.ResourceAuthorityDTO;
import com.behsa.medportal.service.mapper.ResourceAuthorityMapper;
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
 * Integration tests for the {@link ResourceAuthorityResource} REST controller.
 *//*

@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ResourceAuthorityResourceIT {

    private static final String DEFAULT_VERB = "AAAAAAAAAA";
    private static final String UPDATED_VERB = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/resource-authorities";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ResourceAuthorityRepository resourceAuthorityRepository;

    @Autowired
    private ResourceAuthorityMapper resourceAuthorityMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restResourceAuthorityMockMvc;

    private ResourceAuthorityEntity resourceAuthorityEntity;

    */
/**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     *//*

    public static ResourceAuthorityEntity createEntity(EntityManager em) {
        ResourceAuthorityEntity resourceAuthorityEntity = new ResourceAuthorityEntity().verb(DEFAULT_VERB);
        // Add required entity
        MedAuthorityEntity medAuthority;
        if (TestUtil.findAll(em, MedAuthorityEntity.class).isEmpty()) {
            medAuthority = MedAuthorityResourceIT.createEntity(em);
            em.persist(medAuthority);
            em.flush();
        } else {
            medAuthority = TestUtil.findAll(em, MedAuthorityEntity.class).get(0);
        }
        resourceAuthorityEntity.setMedAuthority(medAuthority);
        // Add required entity
        ResourceEntity resource;
        if (TestUtil.findAll(em, ResourceEntity.class).isEmpty()) {
            resource = ResourceResourceIT.createEntity(em);
            em.persist(resource);
            em.flush();
        } else {
            resource = TestUtil.findAll(em, ResourceEntity.class).get(0);
        }
        resourceAuthorityEntity.setResource(resource);
        return resourceAuthorityEntity;
    }

    */
/**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     *//*

    public static ResourceAuthorityEntity createUpdatedEntity(EntityManager em) {
        ResourceAuthorityEntity resourceAuthorityEntity = new ResourceAuthorityEntity().verb(UPDATED_VERB);
        // Add required entity
        MedAuthorityEntity medAuthority;
        if (TestUtil.findAll(em, MedAuthorityEntity.class).isEmpty()) {
            medAuthority = MedAuthorityResourceIT.createUpdatedEntity(em);
            em.persist(medAuthority);
            em.flush();
        } else {
            medAuthority = TestUtil.findAll(em, MedAuthorityEntity.class).get(0);
        }
        resourceAuthorityEntity.setMedAuthority(medAuthority);
        // Add required entity
        ResourceEntity resource;
        if (TestUtil.findAll(em, ResourceEntity.class).isEmpty()) {
            resource = ResourceResourceIT.createUpdatedEntity(em);
            em.persist(resource);
            em.flush();
        } else {
            resource = TestUtil.findAll(em, ResourceEntity.class).get(0);
        }
        resourceAuthorityEntity.setResource(resource);
        return resourceAuthorityEntity;
    }

    @BeforeEach
    public void initTest() {
        resourceAuthorityEntity = createEntity(em);
    }

    @Test
    @Transactional
    void createResourceAuthority() throws Exception {
        int databaseSizeBeforeCreate = resourceAuthorityRepository.findAll().size();
        // Create the ResourceAuthority
        ResourceAuthorityDTO resourceAuthorityDTO = resourceAuthorityMapper.toDto(resourceAuthorityEntity);
        restResourceAuthorityMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(resourceAuthorityDTO))
            )
            .andExpect(status().isCreated());

        // Validate the ResourceAuthority in the database
        List<ResourceAuthorityEntity> resourceAuthorityList = resourceAuthorityRepository.findAll();
        assertThat(resourceAuthorityList).hasSize(databaseSizeBeforeCreate + 1);
        ResourceAuthorityEntity testResourceAuthority = resourceAuthorityList.get(resourceAuthorityList.size() - 1);
        assertThat(testResourceAuthority.getVerb()).isEqualTo(DEFAULT_VERB);
    }

    @Test
    @Transactional
    void createResourceAuthorityWithExistingId() throws Exception {
        // Create the ResourceAuthority with an existing ID
        resourceAuthorityEntity.setId(1L);
        ResourceAuthorityDTO resourceAuthorityDTO = resourceAuthorityMapper.toDto(resourceAuthorityEntity);

        int databaseSizeBeforeCreate = resourceAuthorityRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restResourceAuthorityMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(resourceAuthorityDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ResourceAuthority in the database
        List<ResourceAuthorityEntity> resourceAuthorityList = resourceAuthorityRepository.findAll();
        assertThat(resourceAuthorityList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkVerbIsRequired() throws Exception {
        int databaseSizeBeforeTest = resourceAuthorityRepository.findAll().size();
        // set the field null
        resourceAuthorityEntity.setVerb(null);

        // Create the ResourceAuthority, which fails.
        ResourceAuthorityDTO resourceAuthorityDTO = resourceAuthorityMapper.toDto(resourceAuthorityEntity);

        restResourceAuthorityMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(resourceAuthorityDTO))
            )
            .andExpect(status().isBadRequest());

        List<ResourceAuthorityEntity> resourceAuthorityList = resourceAuthorityRepository.findAll();
        assertThat(resourceAuthorityList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllResourceAuthorities() throws Exception {
        // Initialize the database
        resourceAuthorityRepository.saveAndFlush(resourceAuthorityEntity);

        // Get all the resourceAuthorityList
        restResourceAuthorityMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(resourceAuthorityEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].verb").value(hasItem(DEFAULT_VERB)));
    }

    @Test
    @Transactional
    void getResourceAuthority() throws Exception {
        // Initialize the database
        resourceAuthorityRepository.saveAndFlush(resourceAuthorityEntity);

        // Get the resourceAuthority
        restResourceAuthorityMockMvc
            .perform(get(ENTITY_API_URL_ID, resourceAuthorityEntity.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(resourceAuthorityEntity.getId().intValue()))
            .andExpect(jsonPath("$.verb").value(DEFAULT_VERB));
    }

    @Test
    @Transactional
    void getResourceAuthoritiesByIdFiltering() throws Exception {
        // Initialize the database
        resourceAuthorityRepository.saveAndFlush(resourceAuthorityEntity);

        Long id = resourceAuthorityEntity.getId();

        defaultResourceAuthorityShouldBeFound("id.equals=" + id);
        defaultResourceAuthorityShouldNotBeFound("id.notEquals=" + id);

        defaultResourceAuthorityShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultResourceAuthorityShouldNotBeFound("id.greaterThan=" + id);

        defaultResourceAuthorityShouldBeFound("id.lessThanOrEqual=" + id);
        defaultResourceAuthorityShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllResourceAuthoritiesByVerbIsEqualToSomething() throws Exception {
        // Initialize the database
        resourceAuthorityRepository.saveAndFlush(resourceAuthorityEntity);

        // Get all the resourceAuthorityList where verb equals to DEFAULT_VERB
        defaultResourceAuthorityShouldBeFound("verb.equals=" + DEFAULT_VERB);

        // Get all the resourceAuthorityList where verb equals to UPDATED_VERB
        defaultResourceAuthorityShouldNotBeFound("verb.equals=" + UPDATED_VERB);
    }

    @Test
    @Transactional
    void getAllResourceAuthoritiesByVerbIsInShouldWork() throws Exception {
        // Initialize the database
        resourceAuthorityRepository.saveAndFlush(resourceAuthorityEntity);

        // Get all the resourceAuthorityList where verb in DEFAULT_VERB or UPDATED_VERB
        defaultResourceAuthorityShouldBeFound("verb.in=" + DEFAULT_VERB + "," + UPDATED_VERB);

        // Get all the resourceAuthorityList where verb equals to UPDATED_VERB
        defaultResourceAuthorityShouldNotBeFound("verb.in=" + UPDATED_VERB);
    }

    @Test
    @Transactional
    void getAllResourceAuthoritiesByVerbIsNullOrNotNull() throws Exception {
        // Initialize the database
        resourceAuthorityRepository.saveAndFlush(resourceAuthorityEntity);

        // Get all the resourceAuthorityList where verb is not null
        defaultResourceAuthorityShouldBeFound("verb.specified=true");

        // Get all the resourceAuthorityList where verb is null
        defaultResourceAuthorityShouldNotBeFound("verb.specified=false");
    }

    @Test
    @Transactional
    void getAllResourceAuthoritiesByVerbContainsSomething() throws Exception {
        // Initialize the database
        resourceAuthorityRepository.saveAndFlush(resourceAuthorityEntity);

        // Get all the resourceAuthorityList where verb contains DEFAULT_VERB
        defaultResourceAuthorityShouldBeFound("verb.contains=" + DEFAULT_VERB);

        // Get all the resourceAuthorityList where verb contains UPDATED_VERB
        defaultResourceAuthorityShouldNotBeFound("verb.contains=" + UPDATED_VERB);
    }

    @Test
    @Transactional
    void getAllResourceAuthoritiesByVerbNotContainsSomething() throws Exception {
        // Initialize the database
        resourceAuthorityRepository.saveAndFlush(resourceAuthorityEntity);

        // Get all the resourceAuthorityList where verb does not contain DEFAULT_VERB
        defaultResourceAuthorityShouldNotBeFound("verb.doesNotContain=" + DEFAULT_VERB);

        // Get all the resourceAuthorityList where verb does not contain UPDATED_VERB
        defaultResourceAuthorityShouldBeFound("verb.doesNotContain=" + UPDATED_VERB);
    }

    @Test
    @Transactional
    void getAllResourceAuthoritiesByMedAuthorityIsEqualToSomething() throws Exception {
        MedAuthorityEntity medAuthority;
        if (TestUtil.findAll(em, MedAuthorityEntity.class).isEmpty()) {
            resourceAuthorityRepository.saveAndFlush(resourceAuthorityEntity);
            medAuthority = MedAuthorityResourceIT.createEntity(em);
        } else {
            medAuthority = TestUtil.findAll(em, MedAuthorityEntity.class).get(0);
        }
        em.persist(medAuthority);
        em.flush();
        resourceAuthorityEntity.setMedAuthority(medAuthority);
        resourceAuthorityRepository.saveAndFlush(resourceAuthorityEntity);
        Long medAuthorityId = medAuthority.getId();

        // Get all the resourceAuthorityList where medAuthority equals to medAuthorityId
        defaultResourceAuthorityShouldBeFound("medAuthorityId.equals=" + medAuthorityId);

        // Get all the resourceAuthorityList where medAuthority equals to (medAuthorityId + 1)
        defaultResourceAuthorityShouldNotBeFound("medAuthorityId.equals=" + (medAuthorityId + 1));
    }

    @Test
    @Transactional
    void getAllResourceAuthoritiesByResourceIsEqualToSomething() throws Exception {
        ResourceEntity resource;
        if (TestUtil.findAll(em, ResourceEntity.class).isEmpty()) {
            resourceAuthorityRepository.saveAndFlush(resourceAuthorityEntity);
            resource = ResourceResourceIT.createEntity(em);
        } else {
            resource = TestUtil.findAll(em, ResourceEntity.class).get(0);
        }
        em.persist(resource);
        em.flush();
        resourceAuthorityEntity.setResource(resource);
        resourceAuthorityRepository.saveAndFlush(resourceAuthorityEntity);
        Long resourceId = resource.getId();

        // Get all the resourceAuthorityList where resource equals to resourceId
        defaultResourceAuthorityShouldBeFound("resourceId.equals=" + resourceId);

        // Get all the resourceAuthorityList where resource equals to (resourceId + 1)
        defaultResourceAuthorityShouldNotBeFound("resourceId.equals=" + (resourceId + 1));
    }

    */
/**
     * Executes the search, and checks that the default entity is returned.
     *//*

    private void defaultResourceAuthorityShouldBeFound(String filter) throws Exception {
        restResourceAuthorityMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(resourceAuthorityEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].verb").value(hasItem(DEFAULT_VERB)));

        // Check, that the count call also returns 1
        restResourceAuthorityMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    */
/**
     * Executes the search, and checks that the default entity is not returned.
     *//*

    private void defaultResourceAuthorityShouldNotBeFound(String filter) throws Exception {
        restResourceAuthorityMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restResourceAuthorityMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingResourceAuthority() throws Exception {
        // Get the resourceAuthority
        restResourceAuthorityMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingResourceAuthority() throws Exception {
        // Initialize the database
        resourceAuthorityRepository.saveAndFlush(resourceAuthorityEntity);

        int databaseSizeBeforeUpdate = resourceAuthorityRepository.findAll().size();

        // Update the resourceAuthority
        ResourceAuthorityEntity updatedResourceAuthorityEntity = resourceAuthorityRepository
            .findById(resourceAuthorityEntity.getId())
            .get();
        // Disconnect from session so that the updates on updatedResourceAuthorityEntity are not directly saved in db
        em.detach(updatedResourceAuthorityEntity);
        updatedResourceAuthorityEntity.verb(UPDATED_VERB);
        ResourceAuthorityDTO resourceAuthorityDTO = resourceAuthorityMapper.toDto(updatedResourceAuthorityEntity);

        restResourceAuthorityMockMvc
            .perform(
                put(ENTITY_API_URL_ID, resourceAuthorityDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(resourceAuthorityDTO))
            )
            .andExpect(status().isOk());

        // Validate the ResourceAuthority in the database
        List<ResourceAuthorityEntity> resourceAuthorityList = resourceAuthorityRepository.findAll();
        assertThat(resourceAuthorityList).hasSize(databaseSizeBeforeUpdate);
        ResourceAuthorityEntity testResourceAuthority = resourceAuthorityList.get(resourceAuthorityList.size() - 1);
        assertThat(testResourceAuthority.getVerb()).isEqualTo(UPDATED_VERB);
    }

    @Test
    @Transactional
    void putNonExistingResourceAuthority() throws Exception {
        int databaseSizeBeforeUpdate = resourceAuthorityRepository.findAll().size();
        resourceAuthorityEntity.setId(count.incrementAndGet());

        // Create the ResourceAuthority
        ResourceAuthorityDTO resourceAuthorityDTO = resourceAuthorityMapper.toDto(resourceAuthorityEntity);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restResourceAuthorityMockMvc
            .perform(
                put(ENTITY_API_URL_ID, resourceAuthorityDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(resourceAuthorityDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ResourceAuthority in the database
        List<ResourceAuthorityEntity> resourceAuthorityList = resourceAuthorityRepository.findAll();
        assertThat(resourceAuthorityList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchResourceAuthority() throws Exception {
        int databaseSizeBeforeUpdate = resourceAuthorityRepository.findAll().size();
        resourceAuthorityEntity.setId(count.incrementAndGet());

        // Create the ResourceAuthority
        ResourceAuthorityDTO resourceAuthorityDTO = resourceAuthorityMapper.toDto(resourceAuthorityEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restResourceAuthorityMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(resourceAuthorityDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ResourceAuthority in the database
        List<ResourceAuthorityEntity> resourceAuthorityList = resourceAuthorityRepository.findAll();
        assertThat(resourceAuthorityList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamResourceAuthority() throws Exception {
        int databaseSizeBeforeUpdate = resourceAuthorityRepository.findAll().size();
        resourceAuthorityEntity.setId(count.incrementAndGet());

        // Create the ResourceAuthority
        ResourceAuthorityDTO resourceAuthorityDTO = resourceAuthorityMapper.toDto(resourceAuthorityEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restResourceAuthorityMockMvc
            .perform(
                put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(resourceAuthorityDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the ResourceAuthority in the database
        List<ResourceAuthorityEntity> resourceAuthorityList = resourceAuthorityRepository.findAll();
        assertThat(resourceAuthorityList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateResourceAuthorityWithPatch() throws Exception {
        // Initialize the database
        resourceAuthorityRepository.saveAndFlush(resourceAuthorityEntity);

        int databaseSizeBeforeUpdate = resourceAuthorityRepository.findAll().size();

        // Update the resourceAuthority using partial update
        ResourceAuthorityEntity partialUpdatedResourceAuthorityEntity = new ResourceAuthorityEntity();
        partialUpdatedResourceAuthorityEntity.setId(resourceAuthorityEntity.getId());

        restResourceAuthorityMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedResourceAuthorityEntity.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedResourceAuthorityEntity))
            )
            .andExpect(status().isOk());

        // Validate the ResourceAuthority in the database
        List<ResourceAuthorityEntity> resourceAuthorityList = resourceAuthorityRepository.findAll();
        assertThat(resourceAuthorityList).hasSize(databaseSizeBeforeUpdate);
        ResourceAuthorityEntity testResourceAuthority = resourceAuthorityList.get(resourceAuthorityList.size() - 1);
        assertThat(testResourceAuthority.getVerb()).isEqualTo(DEFAULT_VERB);
    }

    @Test
    @Transactional
    void fullUpdateResourceAuthorityWithPatch() throws Exception {
        // Initialize the database
        resourceAuthorityRepository.saveAndFlush(resourceAuthorityEntity);

        int databaseSizeBeforeUpdate = resourceAuthorityRepository.findAll().size();

        // Update the resourceAuthority using partial update
        ResourceAuthorityEntity partialUpdatedResourceAuthorityEntity = new ResourceAuthorityEntity();
        partialUpdatedResourceAuthorityEntity.setId(resourceAuthorityEntity.getId());

        partialUpdatedResourceAuthorityEntity.verb(UPDATED_VERB);

        restResourceAuthorityMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedResourceAuthorityEntity.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedResourceAuthorityEntity))
            )
            .andExpect(status().isOk());

        // Validate the ResourceAuthority in the database
        List<ResourceAuthorityEntity> resourceAuthorityList = resourceAuthorityRepository.findAll();
        assertThat(resourceAuthorityList).hasSize(databaseSizeBeforeUpdate);
        ResourceAuthorityEntity testResourceAuthority = resourceAuthorityList.get(resourceAuthorityList.size() - 1);
        assertThat(testResourceAuthority.getVerb()).isEqualTo(UPDATED_VERB);
    }

    @Test
    @Transactional
    void patchNonExistingResourceAuthority() throws Exception {
        int databaseSizeBeforeUpdate = resourceAuthorityRepository.findAll().size();
        resourceAuthorityEntity.setId(count.incrementAndGet());

        // Create the ResourceAuthority
        ResourceAuthorityDTO resourceAuthorityDTO = resourceAuthorityMapper.toDto(resourceAuthorityEntity);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restResourceAuthorityMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, resourceAuthorityDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(resourceAuthorityDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ResourceAuthority in the database
        List<ResourceAuthorityEntity> resourceAuthorityList = resourceAuthorityRepository.findAll();
        assertThat(resourceAuthorityList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchResourceAuthority() throws Exception {
        int databaseSizeBeforeUpdate = resourceAuthorityRepository.findAll().size();
        resourceAuthorityEntity.setId(count.incrementAndGet());

        // Create the ResourceAuthority
        ResourceAuthorityDTO resourceAuthorityDTO = resourceAuthorityMapper.toDto(resourceAuthorityEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restResourceAuthorityMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(resourceAuthorityDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ResourceAuthority in the database
        List<ResourceAuthorityEntity> resourceAuthorityList = resourceAuthorityRepository.findAll();
        assertThat(resourceAuthorityList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamResourceAuthority() throws Exception {
        int databaseSizeBeforeUpdate = resourceAuthorityRepository.findAll().size();
        resourceAuthorityEntity.setId(count.incrementAndGet());

        // Create the ResourceAuthority
        ResourceAuthorityDTO resourceAuthorityDTO = resourceAuthorityMapper.toDto(resourceAuthorityEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restResourceAuthorityMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(resourceAuthorityDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the ResourceAuthority in the database
        List<ResourceAuthorityEntity> resourceAuthorityList = resourceAuthorityRepository.findAll();
        assertThat(resourceAuthorityList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteResourceAuthority() throws Exception {
        // Initialize the database
        resourceAuthorityRepository.saveAndFlush(resourceAuthorityEntity);

        int databaseSizeBeforeDelete = resourceAuthorityRepository.findAll().size();

        // Delete the resourceAuthority
        restResourceAuthorityMockMvc
            .perform(delete(ENTITY_API_URL_ID, resourceAuthorityEntity.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<ResourceAuthorityEntity> resourceAuthorityList = resourceAuthorityRepository.findAll();
        assertThat(resourceAuthorityList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
*/
