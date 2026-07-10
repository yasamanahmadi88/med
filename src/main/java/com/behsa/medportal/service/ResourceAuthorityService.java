package com.behsa.medportal.service;

import com.behsa.medportal.service.dto.ResourceAuthorityDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.behsa.medportal.domain.ResourceAuthorityEntity}.
 */
public interface ResourceAuthorityService {
    /**
     * Save a resourceAuthority.
     *
     * @param resourceAuthorityDTO the entity to save.
     * @return the persisted entity.
     */
    ResourceAuthorityDTO save(ResourceAuthorityDTO resourceAuthorityDTO);

    /**
     * Updates a resourceAuthority.
     *
     * @param resourceAuthorityDTO the entity to update.
     * @return the persisted entity.
     */
    ResourceAuthorityDTO update(ResourceAuthorityDTO resourceAuthorityDTO);

    /**
     * Partially updates a resourceAuthority.
     *
     * @param resourceAuthorityDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<ResourceAuthorityDTO> partialUpdate(ResourceAuthorityDTO resourceAuthorityDTO);

    /**
     * Get all the resourceAuthorities.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ResourceAuthorityDTO> findAll(Pageable pageable);

    /**
     * Get the "id" resourceAuthority.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ResourceAuthorityDTO> findOne(Long id);

    /**
     * Delete the "id" resourceAuthority.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
